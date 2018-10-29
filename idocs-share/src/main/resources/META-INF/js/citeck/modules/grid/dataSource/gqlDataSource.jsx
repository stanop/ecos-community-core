import React from 'react';
import BaseDataSource from './baseDataSource';
import formatterStore from '../formatters/formatterStore';

const DEFAULT_FORMATTER = 'DefaultGqlFormatter';

export default class GqlDataSource extends BaseDataSource {
    constructor(options) {
        super(options);

        this._columns = this._getColumns(this.options.columns);
        this.options.ajax.body = this._getBodyJson(this.options.ajax.body, this._columns);
    }

    getColumns(){
        return this._columns;
    }

    load() {
        let options = this.options;
        return fetch(options.url, options.ajax).then(response => { return response.json();});
    }

    _getColumns(columns){
        columns = columns.map((column, idx) => {
            column.dataField = column.id || this._getIdByIdx(idx);
            column.formatterName = column.formatter;
            column.formatter = this._setFormatter(column.formatter);
            column.text = column.text || column.Header;

            return column;
        });

        return columns;
    }

    _getBodyJson(body, columns){
        let defaultBody = {
            schema: this._getSchema(columns)
        };

        return JSON.stringify({...defaultBody, ...body});
    }

    _getSchema(columns){
        let gqlSchemes = columns.map((column, idx) => {
            let formatter = this._getFormatter(column.formatterName);
            let str = formatter ? formatter.getQueryString() : 'str';

            return `id, ${this._getIdByIdx(idx)}: att(name: "${column.field}") {name, val {${str}}}`;
        });

        return gqlSchemes.join(',');
    }

    _getNestedValue(cell){
        const val = cell ? cell.val[0] : null;
        return val ? val.str : '';
    }

    _setFormatter(columnFormatter){
        let that = this;
        let formatter;
        let Formatter = this._getFormatter(columnFormatter || DEFAULT_FORMATTER);

        if(Formatter){
            formatter = (cell, row) => {
                return <Formatter row = {row} cell = {cell} />
            };
        }else if(typeof columnFormatter === 'function'){
            formatter = (cell) => {
                let elCell = {};
                let oRecord = {};
                let oColumn = {};
                let sData = that._getNestedValue(cell);

                columnFormatter(elCell, oRecord, oColumn, sData);

                return <div dangerouslySetInnerHTML={{__html: elCell.innerHTML}} />;
            };
        }

        return formatter
    }

    _getFormatter(name){
        return formatterStore[name];
    }

    _getIdByIdx(idx){
        return `a${idx}`;
    }

    _getDefaultOptions(){
        const options = {
            columns: [],
            url: undefined,
            ajax: {
                method: 'post',
                headers: {
                    'Content-type': 'application/json; charset=UTF-8'
                },
                credentials: 'include',
                body: {}
            }
        };

        return options;
    }
}
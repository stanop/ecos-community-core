import React from 'react';
import BaseDataSource from './baseDataSource';
import formatterStore from '../formatters/formatterStore';

const DEFAULT_FORMATTER = 'DefaultFormatter';

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
            column.id = column.id || this._getIdByIdx(idx);
            column.formatter = column.formatter || DEFAULT_FORMATTER;
            column.accessor = column.accessor || this._getAccessor(column);
            column.Cell = column.Cell || this._getCell(column);

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
            let formatter = this._getFormatter(column.formatter);
            let str = formatter ? formatter.getQueryString() : 'str';

            return `id, ${this._getIdByIdx(idx)}: att(name: "${column.field}") {name, val {${str}}}`;
        });

        return gqlSchemes.join(',');
    }

    _getAccessor(column){
        return row => {
            const cell = row[column.id];
            const val = cell ? cell.val[0] : null;
            return val ? val.str : '';
        };
    }

    _getCell(column){
        let cell;
        let Formatter = this._getFormatter(column.formatter);

        if(Formatter){
            cell = row => {
                const val = this._getCellValByRow(row);

                return <Formatter row = {row} val = {val} value = {row.value}/>
            };
        }else if(typeof column.formatter === 'function'){
            cell = function(options){
                let elCell = {};
                let oRecord = {};
                let oColumn = {};
                let sData = options.value;

                column.formatter(elCell, oRecord, oColumn, sData);

                return <div dangerouslySetInnerHTML={{__html: elCell.innerHTML}} />;
            };
        }

        return cell
    }

    _getFormatter(name){
        return formatterStore[name];
    }

    _getIdByIdx(idx){
        return `a${idx}`;
    }

    _getCellValByRow(row){
        const data = row.original;
        const column = row.column;
        const cell = data[column.id];

        return (cell && cell.val) ? cell.val : null;
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
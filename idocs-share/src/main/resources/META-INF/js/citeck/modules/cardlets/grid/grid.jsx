import React from 'react';
import NodeCardlet from '../node-cardlet';
import Grid from '../../grid/grid';
import dataSourceStore from '../../grid/dataSource/dataSourceStore';



export default class GridCardlet extends NodeCardlet {
    static fetchData(ownProps, onSuccess, onFailure) {
        require([
            'citeck/components/dynamic-tree/cell-formatters'
        ], function() {
            let controlProps = ownProps.controlProps;
            let htmlId = 'card-details-cardlet_' + ownProps.id + "_grid";
            let gridOptions = eval('(' + controlProps.gridOptions + ')');
            let dataSourceName = gridOptions.dataSourceName.trim();

            const dataSource = new dataSourceStore[dataSourceName](gridOptions);

            let success = (data) => onSuccess({
                data: data,
                columns: dataSource.getColumns(),
                htmlId: htmlId,
                header: Alfresco.util.message(controlProps.header),
                hideTwister: controlProps.hideTwister,
                twisterKey: controlProps.twisterKey
            });

            dataSource.load().then(function (data) {
                let records = data.records || [];
                let recordsData = [];
                for (let i = 0; i < records.length; i++) {
                    recordsData.push(records[i].attributes);
                }
                success(recordsData);
            }).catch(function(){
                success([]);
            });
        });
    }

    componentDidMount() {
        let props = this.props.data;
        let htmlId = props.htmlId;
        let hideTwister = props.hideTwister;

        if (!hideTwister) {
            let twisterKey = props.twisterKey || 'dc';
            Alfresco.util.createTwister(`${htmlId}-heading`, twisterKey);
        }
    }

    render() {
        const props = this.props.data;
        const htmlId = props.htmlId;
        const header = props.header;
        const data = props.data;
        const columns = props.columns;

        return (
            <div id={`${htmlId}-panel`} className="document-children document-details-panel">
                <h2 id={`${htmlId}-heading`} className="thin dark">
                    {header}
                    <span id={`${htmlId}-heading-actions`} className="alfresco-twister-actions" style={{position: 'relative', float: 'right'}}/>
                </h2>

                <div className="panel-body">
                    <Grid
                        keyField = 'id'
                        data = {data}
                        columns = {columns}
                    />
                </div>
            </div>
        );
    }
}

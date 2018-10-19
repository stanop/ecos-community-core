import React, {Component} from 'react';
import ReactTable, { ReactTableDefaults } from 'js/citeck/lib/react-table/react-table';

import 'xstyle!js/citeck/lib/react-table/react-table.css';

export default class Grid extends Component  {
    render() {
        let {column, ...props} = this.props;

        Object.assign(ReactTableDefaults.column, column);

        return (
            <div>
                <ReactTable {...props} />
            </div>
        );
    }
}
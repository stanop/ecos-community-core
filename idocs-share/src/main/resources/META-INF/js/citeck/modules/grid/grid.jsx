import React, {Component} from 'react';
import BootstrapTable from 'js/citeck/lib/react-bootstrap-table-next/react-bootstrap-table-next';

import 'xstyle!js/citeck/lib/react-bootstrap-table-next/react-bootstrap-table.css';

export default class Grid extends Component  {
    render() {
        let props = {
            ...this.props,
            ...{
                noDataIndication: () => 'Нет элементов в списке',
                classes: 'table_table-layout_auto',
                bootstrap4: true,
                bordered: false
            }
        };

        return <BootstrapTable {...props}/>;
    }
}

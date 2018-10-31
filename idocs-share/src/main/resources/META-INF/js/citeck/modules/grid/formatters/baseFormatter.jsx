import React, {Component} from 'react';

export default class BaseFormatter extends Component {
    value(){
        return this.props.cell || '';
    }

    render() {
        return (
            <span>
                {`${this.value()}`}
            </span>
        );
    }
}
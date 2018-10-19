import React, {Component} from 'react';

export default class BaseFormatter extends Component {
    static getQueryString(){
        return 'str';
    }

    value(){
        return this.props.value || '';
    }
}
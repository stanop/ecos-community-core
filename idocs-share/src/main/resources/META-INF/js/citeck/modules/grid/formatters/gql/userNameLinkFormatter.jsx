import React from 'react';
import DefaultGqlFormatter from './defaultGqlFormatter';

export default class UserNameLinkFormatter extends DefaultGqlFormatter {
    static getQueryString(){
        return 'str, userName: att(name: "cm:userName"){val{str}}';
    }

    getUrl(cell){
        const val = cell.val || [];
        const userName = val[0] ? (val[0].userName.val[0] ? val[0].userName.val[0].str : null) : null;
        return userName ? `/share/page/user/${userName}/profile` : null;
    }

    render() {
        const cell = this.props.cell;
        const url = this.getUrl(cell || {});

        return (
            <a href = {url}>
                {this.value(cell)}
            </a>
        );
    }
}
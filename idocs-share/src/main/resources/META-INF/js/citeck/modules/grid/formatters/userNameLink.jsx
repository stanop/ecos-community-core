import React from 'react';
import BaseFormatter from './baseFormatter';

export default class UserNameLink extends BaseFormatter {
    static getQueryString(){
        return 'str, userName: att(name: "cm:userName"){val{str}}';
    }

    getUrl(val){
        const userName = val[0] ? (val[0].userName.val[0] ? val[0].userName.val[0].str : null) : null;
        return userName ? `/share/page/user/${userName}/profile` : null;
    }

    render() {
        const url = this.getUrl(this.props.val || []);

        return (
            <a href = {url}>
                {this.value()}
            </a>
        );
    }
}
import React from 'ecosui!react';
import DefaultGqlFormatter from './defaultGqlFormatter';

export default class UserNameLink extends DefaultGqlFormatter {
    static getQueryString(attribute) {
        return `.att(n:"${attribute}"){displayName:disp,userName:att(n:"cm:userName"){str}}`;
    }

    value(cell) {
        return cell.displayName || '';
    }

    render() {
        let props = this.props;
        let cell = props.cell || {};
        let userName = cell.userName || '';

        return <a href={`/share/page/user/${userName}/profile`}>{this.value(cell)}</a>;
    }
}
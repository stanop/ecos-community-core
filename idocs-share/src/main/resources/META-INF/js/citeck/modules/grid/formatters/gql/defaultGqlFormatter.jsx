import React from 'ecosui!react';
import BaseFormatter from '../baseFormatter';

export default class DefaultGqlFormatter extends BaseFormatter {
    static getQueryString(attribute) {
        return `${attribute}?disp`;
    }
}
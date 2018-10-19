import React from 'react';
import BaseFormatter from './baseFormatter';

export default class DefaultFormatter extends BaseFormatter {
    render() {
        return (
            <span>
                {this.value()}
            </span>
        );
    }
}
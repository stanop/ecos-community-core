import React from 'react';
import { connect } from "react-redux";
import { loadMenuItemIconUrl } from '../actions';

const mapDispatchToProps = (dispatch, ownProps) => ({
    loadMenuItemIconUrl: (webScriptUrl, cb) => dispatch(loadMenuItemIconUrl(webScriptUrl, cb))
});

class ListItemIconImg extends React.Component {
    state = {
        url: null,
    };

    componentDidMount() {
        const { webScriptUrl, loadMenuItemIconUrl } = this.props;
        loadMenuItemIconUrl(webScriptUrl, (url) => this.setState({ url }) );
    }

    render() {
        const { url } = this.state;
        const backgroundImage = url ? `url(${url})` : null;

        return (
            <div className='list-item-icon-img' style={{backgroundImage}} />
        );
    }
}

export default connect(null, mapDispatchToProps)(ListItemIconImg);


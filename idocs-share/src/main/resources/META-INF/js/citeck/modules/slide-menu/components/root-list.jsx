import React from 'ecosui!react';
import { connect } from "ecosui!react-redux";
import { Scrollbars } from 'ecosui!react-custom-scrollbars';
import List from './list';
import {t} from "../../common/util";

const mapStateToProps = (state) => ({
    items: state.leftMenu.items,
    scrollTop: state.leftMenu.scrollTop,
});

class RootList extends React.Component {
    componentDidUpdate(prevProps) {
        if (this.props.scrollTop !== prevProps.scrollTop) {
            this.scrollbar.scrollTop(this.props.scrollTop);
        }
    }

    render() {
        const { items, toggleSlideMenu } = this.props;

        const scrollBarStyle = { height: 'calc(100% - 40px)' };
        const verticalTrack = props => <div {...props} className="slide-menu-list__vertical-track" />;

        const rootListItems = items.map(item => {
            const nestedList = <List items={item.items} toggleSlideMenu={toggleSlideMenu} isExpanded />;

            return (
                <li key={item.id} id={item.id} className="slide-menu-list__item list-divider">
                    <span className="list-divider__text">{t(item.label)}</span>
                    {nestedList}
                </li>
            );
        });

        return (
            <Scrollbars ref={el => this.scrollbar = el} className="slide-menu-list" autoHide style={scrollBarStyle} renderTrackVertical={verticalTrack}>
                <nav>
                    <ul className="slide-menu-collapsible-list slide-menu-collapsible-list_expanded">{rootListItems}</ul>
                </nav>
            </Scrollbars>
        );
    }
}

export default connect(mapStateToProps)(RootList);
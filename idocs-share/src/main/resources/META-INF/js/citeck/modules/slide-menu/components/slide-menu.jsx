import React, {Fragment} from 'react';
import ReactDOM from "react-dom";
import { connect } from "react-redux";
import LogoBlock from "./logo-block";
import RootList from "./root-list";

const mapStateToProps = (state) => ({
    items: state.leftMenu.items,
    smallLogo: state.leftMenu.smallLogo,
    largeLogo: state.leftMenu.largeLogo
});

class SlideMenu extends React.Component {
    constructor(props) {
        super(props);

        this.checkbox = document.createElement('input');
        this.checkbox.id = 'slide-menu-checkbox';
        this.checkbox.type = 'checkbox';

        this.menu = document.createElement('div');
        this.menu.classList.add('slide-menu');

        this.mask = document.createElement('div');
        this.mask.classList.add('slide-menu-mask');

        const maskLabel = document.createElement('label');
        maskLabel.setAttribute("for", "slide-menu-checkbox");
        this.mask.appendChild(maskLabel);
    }

    componentDidMount() {
        const theFirstChild = document.body.firstChild;
        document.body.insertBefore(this.checkbox, theFirstChild);
        document.body.insertBefore(this.menu, theFirstChild);
        document.body.insertBefore(this.mask, theFirstChild);

        // this.checkbox.addEventListener('change', this.onCheckboxChange);
    }

    componentWillUnmount() {
        // this.checkbox.removeEventListener('change', this.onCheckboxChange);

        document.body.removeChild(this.checkbox);
        document.body.removeChild(this.menu);
        document.body.removeChild(this.mask);
    }

    onCheckboxChange = e => {
        // console.log(e.target.checked);
    };

    toggleSlideMenu = () => {
        this.slideMenuToggle && this.slideMenuToggle.click();
    };

    render() {
        const { items, smallLogo, largeLogo } = this.props;

        return ReactDOM.createPortal(
            <Fragment>
                <label
                    ref={el => this.slideMenuToggle = el}
                    className='slide-menu-toggle'
                    htmlFor="slide-menu-checkbox"
                />
                <LogoBlock smallLogo={smallLogo} largeLogo={largeLogo} />
                <RootList toggleSlideMenu={this.toggleSlideMenu} items={items} />
            </Fragment>,
            this.menu,
        );
    }
}

export default connect(mapStateToProps)(SlideMenu);
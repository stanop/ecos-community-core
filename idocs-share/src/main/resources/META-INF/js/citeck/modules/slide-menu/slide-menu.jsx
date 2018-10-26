import React, {Fragment} from 'react';
import ReactDOM from "react-dom";
import "xstyle!./slide-menu.css";
import { LogoBlock, ListBlock } from "./components";

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
        // TODO rid of this.props.slideMenuConfig
        const slideMenuConfig = this.props.slideMenuConfig;
        const smallLogo = slideMenuConfig.logoSrcMobile;
        const largeLogo = slideMenuConfig.logoSrc;

        console.log('slideMenuConfig', slideMenuConfig);

        return ReactDOM.createPortal(
            <Fragment>
                <label
                    ref={el => this.slideMenuToggle = el}
                    className='slide-menu-toggle'
                    htmlFor="slide-menu-checkbox"
                />
                <LogoBlock smallLogo={smallLogo} largeLogo={largeLogo} />
                <ListBlock toggleSlideMenu={this.toggleSlideMenu} items={slideMenuConfig.widgets} />
            </Fragment>,
            this.menu,
        );
    }
}

export const render = (elementId, props) => {
    ReactDOM.render(
        <SlideMenu { ...props } />,
        document.getElementById(elementId)
    );
};
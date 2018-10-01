import React from 'react';
import ReactDOM from 'react-dom';
import { connect } from "react-redux";
import { Button, Modal } from 'react-bootstrap';
import { hideModal } from "../actions";

const mapStateToProps = state => ({
    ...state.modal
});

const mapDispatchToProps = dispatch => ({
    hideModal: () => dispatch(hideModal())
});

class CustomModal extends React.Component {
    constructor(props) {
        super(props);
        this.el = document.createElement('div');
    }

    componentDidMount() {
        document.body.appendChild(this.el);
    }

    componentWillUnmount() {
        document.body.removeChild(this.el);
    }

    render() {
        const { isOpen, title, content, buttons, onCloseCallback, hideModal } = this.props;

        let onHideCallback = hideModal;
        if (onCloseCallback) {
            onHideCallback = () => {
                onCloseCallback();
                hideModal();
            }
        }

        if (!isOpen) {
            return null;
        }

        const header = title ? (
            <Modal.Header closeButton>
                <Modal.Title>{title}</Modal.Title>
            </Modal.Header>
        ) : null;

        let footer = null;
        if (Array.isArray(buttons) && buttons.length > 0) {
            const buttonList = buttons.map((button, idx) => {
                let onButtonClick = button.onClick;
                if (button.isCloseButton) {
                    onButtonClick = onHideCallback;
                }
                return (
                    <Button
                        key={idx}
                        onClick={onButtonClick}
                        bsStyle={button.bsStyle}
                    >
                        {button.label}
                    </Button>
                );
            });

            footer = <Modal.Footer>{buttonList}</Modal.Footer>;
        }

        return ReactDOM.createPortal(
            <Modal show onHide={onHideCallback}>
                {header}
                <Modal.Body>{content}</Modal.Body>
                {footer}
            </Modal>,
            this.el,
        );
    }
}

export default connect(mapStateToProps, mapDispatchToProps)(CustomModal);
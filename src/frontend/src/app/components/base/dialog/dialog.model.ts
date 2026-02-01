export enum DialogType {
    OK = 'OK',
    YES_NO = 'YES_NO',
    YES_NO_CANCEL = 'YES_NO_CANCEL'
}

export enum DialogResult {
    OK = 'OK',
    YES = 'YES',
    NO = 'NO',
    CANCEL = 'CANCEL'
}

export interface DialogConfig {
    title: string;
    message: string;
    type: DialogType;
    okLabel?: string;
    yesLabel?: string;
    noLabel?: string;
    cancelLabel?: string;
}

export interface DialogButton {
    label: string;
    result: DialogResult;
    cssClass: string;
}

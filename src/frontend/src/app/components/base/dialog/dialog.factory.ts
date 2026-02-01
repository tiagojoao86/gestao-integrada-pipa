import { DialogService } from './dialog.service';

export class DialogServiceFactory {
    private static instance: DialogService;

    public static create() {
        if (!this.instance) this.instance = new DialogService();

        return this.instance;
    }
}

export const dialogServiceProvider = () => {
    return DialogServiceFactory.create();
};

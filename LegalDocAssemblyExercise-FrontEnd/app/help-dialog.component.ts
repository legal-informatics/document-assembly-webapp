import {Component, Input, Output, EventEmitter} from "@angular/core";
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {Exercise} from './viewmodels/exercise';
import {ExerciseService} from './services/exercise.service';
import {TranslateService} from "ng2-translate";

@Component({

    selector: 'help-dialog',
    template: `
    <p-dialog [(visible)]="display" [modal]="true" width="550" [responsive]="true">
        <header>Help</header>
        <p>{{'HelpDialog.Text'|translate}}</p>
        <footer>
            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <button type="button" pButton icon="fa-check" (click)="confirmed()" label="{{'HelpDialog.Close'|translate}}"></button>
            </div>
        </footer>
    </p-dialog>
    `
})

export class HelpDialogComponent {

    display: boolean = false;
    
    constructor() {
        
    }

    confirmed() {
        this.display = false;
    }
    canceled() {
        this.display = false;
    }

    showDialog() {
        this.display = true;
    }
    
}
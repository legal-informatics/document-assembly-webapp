import {Component, Input, Output, EventEmitter} from "@angular/core";
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {Exercise} from './viewmodels/exercise';
import {ExerciseService} from './services/exercise.service';


@Component({

    selector: 'help-dialog',
    template: `
    <p-dialog [(visible)]="display" [modal]="true" width="550" [responsive]="true">
        <header>Help</header>
        <p>Legal Document Assembly Exercise System is a prototype application developed as a proof of concept for PhD thesis.</p>
        <p>As user choose an exercise based on rule-base and document template, while progressing through fact gathering process, system shows how those facts reflects on document content and corresponding argumentation.</p>
        <footer>
            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <button type="button" pButton icon="fa-check" (click)="confirmed()" label="Close"></button>
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
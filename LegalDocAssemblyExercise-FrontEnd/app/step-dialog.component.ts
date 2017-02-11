import {Component, Input, Output, EventEmitter} from "@angular/core";
import {Step} from './viewmodels/step';
import {DialogModule} from 'primeng/primeng';
import {MessagesModule} from 'primeng/primeng';
import {Message} from 'primeng/primeng';

@Component({

    selector: 'step-dialog',
    template: `
    <p-dialog [(visible)]="display" width="550" responsive="true" [modal]="false" showHeader="false" [closable]="false" [appendTo]="scrollableProgressPanel">
        <!-- <header>Please enter the following data</header> -->
        <p-messages [value]="messages"></p-messages>
        <label>{{stepText}}:</label>
        <input id="in" type="text" size="30" pInputText [(ngModel)]="answer" (keyup.enter)="confirmed()">
        <footer>
            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <button type="button" pButton icon="fa-chevron-right" (click)="confirmed()" label="Next"></button>
                <button type="button" pButton icon="fa-chevron-left" (click)="previous()" label="Previous"></button>
                <button type="button" pButton icon="fa-close" (click)="canceled()" label="Cancel"></button>
            </div>
        </footer>
    </p-dialog>
    `
})

export class StepDialogComponent {

    stepText: string = "";
    answer: string = "";
    display: boolean = false;
    messages: Message[] = [];

    @Input() step: Step;
    @Output() onConfirm = new EventEmitter();
    @Output() onCancel = new EventEmitter();
    @Output() onPrevious = new EventEmitter();

    constructor() { }

    confirmed() {
        if (this.validate()) {
            //this.display = false;
            this.onConfirm.emit({
                value: this.answer
            })
        } else {
            this.messages.length = 0;
            this.messages.push({severity:'error', summary:'Error Message', detail:'Wrong data type'});
        }
    }
    canceled() {
        //this.display = false;
        this.onCancel.emit();
    }
    previous() {
        this.onPrevious.emit();
    }

    showDialog(step: Step, hasPrevious: boolean) {
        this.step = step;
        this.stepText = step.text;
        this.answer = step.answer;
        this.display = true;
        this.messages = [];
    }
    hideDialog() {
        this.display = false;
    }

    private regExp_int: RegExp = new RegExp('^[0-9]+$');
    private regExp_float: RegExp = new RegExp('^[-+]?[0-9]*\\.?[0-9]+$');
    private regExp_string: RegExp = new RegExp('.*');
    private regExp_date: RegExp = new RegExp('^(0?[1-9]|[12][0-9]|3[01])\\.(0?[1-9]|1[012])\\.(19|20)\\d\\d\\.$');
    private regExp_time: RegExp = new RegExp('^(0?[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$');
    validate():boolean {
        if (this.step.answerType == "string") {
            return this.regExp_string.test(this.answer);
        } else if (this.step.answerType == "int") {
            return this.regExp_int.test(this.answer);
        } else if (this.step.answerType == "float") {
            return this.regExp_float.test(this.answer);
        } else if (this.step.answerType == "date") {
            return this.regExp_date.test(this.answer);
        } else if (this.step.answerType == "time") {
            return this.regExp_time.test(this.answer);
        } else if (this.step.answerType.indexOf("enum") == 0) {
            let possibleValues: string[] = this.step.answerType.substring(
                this.step.answerType.indexOf("(") + 1,
                this.step.answerType.indexOf(")")).split("|");
            return possibleValues.indexOf(this.answer) > -1;
        }
        return true;
    }
}
import {Component, ViewChild, Input, Output, EventEmitter, ChangeDetectorRef} from "@angular/core";
import {Step} from './viewmodels/step';
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {OverlayPanelModule, OverlayPanel} from 'primeng/primeng';
import {MessagesModule} from 'primeng/primeng';
import {Message} from 'primeng/primeng';
import {TranslateService} from "ng2-translate";

@Component({

    selector: 'step-dialog',
    template: `
    <p-overlayPanel #overlayPanel id="dialogPanel" [dismissable]="false" [style]="{'background-color': '#f0f0f0', 'border': '1px solid black'}">
        <div style="width: 300px; margin-top:5px;">   <!-- width: 600px; margin-left: 10px; -->
            <p-messages [value]="messages"></p-messages>
            <label>{{stepText}}:</label>
            <input *ngIf="!showComboBox" #inputField type="text" size="30" pInputText [(ngModel)]="answer" (keyup.enter)="confirmed()" (keyup.escape)="canceled()" placeholder="{{hint}}" style="width: 136px;">
            <p-dropdown *ngIf="showComboBox" #comboBox [options]="possibleAnswers" [(ngModel)]="answer" [style]="{'width':'200px','vertical-align':'middle'}"></p-dropdown>
            <label>{{answerSuffix}}</label>
            <div style="height: 7px"></div>
            <div style="text-align: right;">
                <button type="button" pButton icon="fa-close" (click)="canceled()" label="{{'StepDialog.Cancel'|translate}}" style="font-size: 11pt;"></button>
                <button type="button" pButton icon="fa-chevron-left" (click)="previous()" label="{{'StepDialog.Previous'|translate}}" [disabled]="!hasPreviousStep" style="font-size: 11pt;"></button>
                <button type="button" pButton icon="fa-chevron-right" (click)="confirmed()" label="{{'StepDialog.Next'|translate}}" style="font-size: 11pt;"></button>
            </div>
        </div>
    </p-overlayPanel>
    `,
    styles: [`.ui-overlaypanel-content { padding: 0px; }`]  // button span { font-size: 12pt; }
})

export class StepDialogComponent {

    stepText: string = "";
    answer: string = "";
    answerSuffix: string = "";
    hint: string = "";
    messages: Message[] = [];
    hasPreviousStep: boolean = true;
    
    showComboBox: boolean = false;
    possibleAnswers: SelectItem[] = [];
    selectedAnswer: String;

    @ViewChild('overlayPanel')
    overlayPanel: OverlayPanel;
    @ViewChild('inputField')
    inputField;
    @ViewChild('comboBox')
    comboBox;
    
    // @Input()
    step: Step;
    @Output() onConfirm = new EventEmitter();
    @Output() onCancel = new EventEmitter();
    @Output() onPrevious = new EventEmitter();

    constructor(public changeDetectorRef: ChangeDetectorRef) {
        
    }

    confirmed() {
        if (this.validate()) {
            this.onConfirm.emit({
                value: this.answer
            });
        } else {
            this.messages.length = 0;
            this.messages.push({severity:'error', summary:'Error Message', detail:'Wrong data type'});
        }
    }
    canceled() {
        this.onCancel.emit();
    }
    previous() {
        this.onPrevious.emit();
    }

    showDialog(step: Step, hasPreviousStep: boolean, positioningElement: any) {
        this.step = step;
        this.stepText = step.text;
        this.answer = step.answer;
        this.answerSuffix = step.answerSuffix;
        this.hint = step.hint;
        this.hasPreviousStep = hasPreviousStep;
        this.messages = [];
        this.possibleAnswers.length = 0;
        var focusElement: any = undefined;
        
        if (this.step.answerType.indexOf("enum") == 0) {
            var values:string[] = this.step.answerType.substring(
                this.step.answerType.indexOf("(") + 1,
                this.step.answerType.indexOf(")")).split(",");
            for (let item of values) {
                this.possibleAnswers.push({label: item, value: item})
            }
            if (values.length > 0) {
                this.answer = values[0];
            }
            this.showComboBox = true;
            focusElement = this.comboBox;
        } else {
            this.showComboBox = false;
            focusElement = this.inputField;
        }

        this.overlayPanel.show(null, positioningElement);
        this.changeDetectorRef.detectChanges();
        if (focusElement != undefined)
            focusElement.nativeElement.focus();
    }
    hideDialog() {
        this.overlayPanel.hide();
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
                this.step.answerType.indexOf(")")).split(",");
            return possibleValues.indexOf(this.answer) > -1;
        }
        return true;
    }
}
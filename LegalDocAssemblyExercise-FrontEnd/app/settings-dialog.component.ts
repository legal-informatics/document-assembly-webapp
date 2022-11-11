import {Component, ViewChild, Input, Output, EventEmitter, ChangeDetectorRef} from "@angular/core";
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {OverlayPanelModule, OverlayPanel} from 'primeng/primeng';
import {MessagesModule} from 'primeng/primeng';
import {Message} from 'primeng/primeng';
import {Settings} from './models/settings';
import {TranslateService} from "ng2-translate";

@Component({

    selector: 'settings-dialog',
    template: `
    <p-dialog #overlayPanel [(visible)]="display" [modal]="true" showHeader="false" [closable]="false" [closeOnEscape]="false" width="650" [responsive]="true">
        <div style="width: 600px; margin-top:10px; margin-left: 10px;">
            <p-checkbox *ngIf="settings!=undefined" [(ngModel)]="settings.revisionClearsFollowingAnswers" binary="true" label="{{'SettingsDialog.RevisionClearsFollowingAnswers'|translate}}"></p-checkbox>
            <br/>
            <p-checkbox *ngIf="settings!=undefined" [(ngModel)]="settings.revisionConsidersFollowingAnswers" binary="true" label="{{'SettingsDialog.RevisionConsidersFollowingAnswers'|translate}}"></p-checkbox>
            
            <div style="height:20px"></div>
            <div style="text-align: right;">
                <!--
                <button type="button" pButton icon="fa-close" (click)="canceled()" label="{{'SettingsDialog.Cancel'|translate}}"></button>
                -->
                <button type="button" pButton icon="fa-check" (click)="confirmed()" label="{{'SettingsDialog.Ok'|translate}}"></button>
            </div>
        </div>
    </p-dialog>
    `
})

export class SettingsDialogComponent {

    display: boolean = false;
    settings: Settings;

    @ViewChild('overlayPanel')
    overlayPanel: OverlayPanel;
    @ViewChild('inputField')
    inputField;
    @ViewChild('comboBox')
    comboBox;
    
    // @Input()
    @Output() onConfirm = new EventEmitter();
    @Output() onCancel = new EventEmitter();

    constructor(public changeDetectorRef: ChangeDetectorRef) {
        
    }

    confirmed() {
        this.onConfirm.emit({
            value: this.settings
        })
    }
    canceled() {
        this.display = false;
        //this.onCancel.emit();
    }

    showDialog(settings: Settings) {
        this.settings = settings;
        this.display = true;
    }
    
    hideDialog() {
        this.display = false;
    }

}
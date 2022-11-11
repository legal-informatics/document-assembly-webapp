import {Component, Input, Output, EventEmitter} from "@angular/core";
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {Exercise} from './viewmodels/exercise';
import {ExerciseService} from './services/exercise.service';
import {TranslateService} from "ng2-translate";

const defaultLanguage = "sr";
const additionalLanguages = ["en"];
const languages: string[] = [defaultLanguage].concat(additionalLanguages);

@Component({

    selector: 'login-dialog',
    template: `
    <p-dialog [(visible)]="display" [modal]="true" showHeader="false" [closable]="false" [closeOnEscape]="false" width="720" [responsive]="true">
        <div style="display: flex; align-items: center; margin-top: 10px;">
            <span style="margin-right: 10px;">{{"LoginDialog.ExerciseSelection"|translate}}:</span>
            <p-dropdown [options]="dropdownItems" [(ngModel)]="selectedExercise"  [style]="{'width':'450px'}"></p-dropdown>   <!--  -->
        </div>
        <p style="margin-top: 50px">{{"LoginDialog.LanguageSelection"|translate}}:
            <select #langSelect (change)="translate.use(langSelect.value)">
                <option *ngFor="let lang of translate.getLangs()" [value]="lang" [selected]="lang === translate.currentLang">{{lang}}</option>
            </select>
        </p>
        <!-- <div style="margin-bottom: 10px"></div> -->
        <footer>
            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <button type="button" pButton icon="fa-check" (click)="confirmed()" label="{{'LoginDialog.Confirm'|translate}}"></button>
            </div>
        </footer>
    </p-dialog>
    `
})

export class LoginDialogComponent {

    display: boolean = false;
    
    dropdownItems: SelectItem[];
//    dropdownSelection: SelectItem;
    exercises: Exercise[];
    selectedExercise: Exercise;

    @Output() onConfirm = new EventEmitter();
    @Output() onCancel = new EventEmitter();

    constructor(public exerciseService: ExerciseService,
        private translate: TranslateService) {
        
        this.translate.addLangs(languages);
        this.translate.setDefaultLang(defaultLanguage);
        // sets default language according to browser settings
/*
        let initLang = this.translate.getBrowserLang();
        if (languages.indexOf(initLang) === -1) {
            initLang = defaultLanguage;
        }
        this.translate.use(initLang);
*/
        this.translate.use(defaultLanguage);
        
        this.dropdownItems = [];
        this.dropdownItems.push({ label: 'Select exercise', value: null });

        this.exerciseService.getExercises().subscribe((result: Exercise[]) => {
            this.exercises = result;
            
            this.dropdownItems.length = 0;
            for (let i = 0; i < this.exercises.length; i++) {
                this.dropdownItems.push({ label: this.exercises[i].name, value: this.exercises[i] });
                if (i==0) {
                    this.selectedExercise = this.exercises[i];
                }
            }
        });
    }

    confirmed() {
        this.onConfirm.emit({
            value: this.selectedExercise
        })
    }
    canceled() {
        this.display = false;
        this.onCancel.emit();
    }

    showDialog() {
        this.display = true;
    }
    
    hideDialog() {
        this.display = false;
    }
    
}
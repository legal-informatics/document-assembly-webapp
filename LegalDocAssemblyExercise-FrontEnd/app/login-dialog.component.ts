import {Component, Input, Output, EventEmitter} from "@angular/core";
import {DialogModule} from 'primeng/primeng';
import {SelectItem} from "primeng/primeng";
import {Exercise} from './viewmodels/exercise';
import {ExerciseService} from './services/exercise.service';


@Component({

    selector: 'login-dialog',
    template: `
    <p-dialog [(visible)]="display" [modal]="true" [closable]="false" [closeOnEscape]="false" width="550" [responsive]="true">
        <!-- header>Log in</header -->
        <p>Choose an exercise:</p>
        <p-dropdown [options]="dropdownItems" [(ngModel)]="selectedExercise" [style]="{'width':'90%'}"></p-dropdown>
        <div style="margin-bottom: 150px"></div>
        <footer>
            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <button type="button" pButton icon="fa-check" (click)="confirmed()" label="Confirm"></button>
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

    constructor(public exerciseService: ExerciseService) {
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
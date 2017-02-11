import {Component, ViewChild, ElementRef, ChangeDetectorRef} from '@angular/core';
import {DomSanitizer, SafeResourceUrl, SafeUrl} from '@angular/platform-browser';
import {StepDialogComponent} from "./step-dialog.component";
import {LoginDialogComponent} from "./login-dialog.component";
import {HelpDialogComponent} from "./help-dialog.component";
import {Button} from "primeng/primeng";
import {SelectItem} from "primeng/primeng";
import {TooltipModule} from "primeng/primeng";
import {BlockUIModule} from "primeng/primeng";
import {Exercise} from './viewmodels/exercise';
import {Step} from './viewmodels/step';
import {ExplanatoryMaterial} from './viewmodels/explanatoryMaterial';
import {ExerciseService} from './services/exercise.service';
import {AssemblyService} from './services/assembly.service';
import {ExplanatoryMaterialService} from './services/explanatoryMaterial.service';

@Component({
    selector: "exercise-component",
    // directives: [Button],
    template: `
 
<login-dialog #loginDialog (onConfirm)="loginConfirmed($event)"></login-dialog>
<button pButton type="text" (click)="closeExercise()" icon="fa-close" style="position: absolute; top: 5px; right: 5px;"></button>
<button pButton type="text" (click)="showHelp()" icon="fa-question" style="position: absolute; top: 5px; right: 45px;"></button>\n\
<help-dialog #helpDialog></help-dialog>
<div>
    <p-panel header="Exercise progress {{selectedExercise!=null ? (' - '+selectedExercise.name) : ''}}" [style]="{'margin':'10px'}">
        <div #scrollableProgressPanel style="overflow-x: scroll; white-space: nowrap; overflow-y: hidden; height: 55px;">
            <div *ngFor="let step of stepsSoFar; let i=index" style="display: inline;">
                <button pButton type="text" (click)="jumpOn(i)" label="Step {{i+1}}" [style.background]="step.underRevision ? 'gray' : '#2399e5'" pTooltip="{{step.text}}" tooltipPosition="top"></button>
                <i *ngIf="i<stepsSoFar.length-1" class="fa fa-arrow-right" aria-hidden="true"></i>
            </div>
        </div>
        <step-dialog #stepDialog [step]="currentStep" (onConfirm)="dilaogConfirmed($event)" (onCancel)="dilaogCanceled($event)" (onPrevious)="dialogPrevious($event)"></step-dialog>
    </p-panel>
<div>
    <div style="float:left; width:50%">
        <p-panel #panelDocument header="Document" [style]="{'margin':'0 0 10px 10px'}">
            <div [innerHTML]="document" style="height: calc(100vh - 230px); width: 100%; resize: none; overflow: scroll;"></div>
            <!-- textarea pInputTextarea [(ngModel)]="document" style="height: calc(100vh - 230px); width: 100%; resize: none;"></textarea -->
        </p-panel>
    </div>
    <div style="float:right; width:50%;">
        <div style="padding: 0 0 0 15px">
            <p-panel #panelArgumentGraph header="Argument graph" [toggleable]="true" (onBeforeToggle)="panelArgumentGraphToggle($event,true)" (onAfterToggle)="panelArgumentGraphToggle($event,false)" [style]="{'margin':'0 10px 10px 0'}">
                <div id="graphDiv" style="height: 250px; overflow: scroll;">
                    <img *ngIf="argumentGraphUrl != null" [src]='argumentGraphUrl' (load)="onLoadArgumentGraph()"/>
                </div>
            </p-panel>
            <p-panel #panelExplanatoryMaterial header="Explanatory material" [toggleable]="true" (onBeforeToggle)="panelExplanatoryToggle($event,true)" (onAfterToggle)="panelExplanatoryToggle($event,false)" [style]="{'margin':'0 10px 0 0'}">
                <div id="explanatoryDiv" style="height: calc(100vh - 540px); overflow: scroll;">
                    <p-tabView>
                        <p-tabPanel *ngFor="let explanation of currentStepExplanations; let i=index" [header]="explanation.title">
                          <div [innerHTML]="domSanitizer.bypassSecurityTrustHtml(explanation.content)"></div>
                        </p-tabPanel>
                    </p-tabView>
                </div>
            </p-panel>
            <p-blockUI [blocked]="blockedGUI"></p-blockUI>
        </div>
    </div>
</div>
        `
})
export class ExerciseComponent {

    @ViewChild('loginDialog')
    loginDialog: LoginDialogComponent;
    @ViewChild('helpDialog')
    helpDialog: HelpDialogComponent;
    @ViewChild('stepDialog')
    stepDialog: StepDialogComponent;
    @ViewChild('scrollableProgressPanel')
    scrollableProgressPanel: ElementRef;

    dropdownItems: SelectItem[];
    dropdownSelection: SelectItem;
    exercises: Exercise[];
    selectedExercise: Exercise;
    stepsSoFar: Step[] = new Array<Step>();
    currentStep: Step;
    nextStep: Step;
    currentStepIndex: number;
    assemblyId: number;
    document: any;
    currentStepExplanations: ExplanatoryMaterial[] = new Array<ExplanatoryMaterial>();
    argumentGraphUrl: string;
    
    blockedGUI: boolean = false;
    documentUpdated: boolean = false;
    argumentGraphUpdated: boolean = false;
    explanatoryMaterialUpdated: boolean = false;

    constructor(public exerciseService: ExerciseService,
                public assemblyService: AssemblyService,
                public explanatoryMaterialService: ExplanatoryMaterialService,
                public changeDetectorRef: ChangeDetectorRef,
                private domSanitizer: DomSanitizer) {
        this.dropdownItems = [];
        this.dropdownItems.push({ label: 'Select exercise', value: null });

        this.changeDetectorRef = changeDetectorRef;
    }

    public loginConfirmed(event) {
        this.selectedExercise = event.value;
        this.stepsSoFar.length = 0;
        this.document = "";
        this.argumentGraphUrl = null;
        this.currentStepExplanations.length = 0;
        if (this.selectedExercise != null) {
            for (let i=0; i<this.selectedExercise.Steps.length; i++) {
                this.selectedExercise.Steps[i].answer = null; // clears answers if same exercise was already answered
            }
            this.assemblyService.postAssembly(this.selectedExercise).subscribe(result => {
                this.assemblyId = parseInt(result.toString());
                this.loginDialog.hideDialog();
                if (this.selectedExercise.Steps != null) {
                    if (this.selectedExercise.Steps.length > 0) {
                        this.currentStep = this.selectedExercise.Steps[0];
                        this.stepsSoFar.push(this.currentStep);
                        this.currentStepIndex = 0;
                        this.stepDialog.showDialog(this.currentStep, false);
                        this.updateExplanatoryMaterial(false);
                    }
                }
            });
        }
    }

    public closeExercise() {
        this.stepsSoFar.length = 0;
        this.stepDialog.hideDialog();
        this.selectedExercise = null;
        this.loginDialog.showDialog();
    }

    public dilaogConfirmed(event) {
        let oldValue: string = this.currentStep.answer;
        let newValue: string = event.value;
        this.nextStep = null;
        if (oldValue == newValue) {  // same answer
            if (this.currentStepIndex + 1 < this.stepsSoFar.length) { // revise next step
                this.stepDialog.hideDialog();
                this.currentStep = this.stepsSoFar[++this.currentStepIndex];
                this.currentStep.underRevision = false;
                this.stepDialog.showDialog(this.currentStep, true);
                this.updateExplanatoryMaterial(false);
                // this.updateArgumentGraph();
            } else if (this.selectedExercise.Steps.length > this.currentStepIndex+1) {
                this.nextStep = this.selectedExercise.Steps[++this.currentStepIndex];
            }
        } else {  // different answer
            this.currentStep.answer = newValue;
            this.stepsSoFar.length = this.currentStepIndex + 1; // cuts the rest of steps
            for (let i=this.currentStepIndex+1; i<this.selectedExercise.Steps.length; i++) {
                this.selectedExercise.Steps[i].answer = null; // clears answers
            }
            if (this.selectedExercise.Steps.length > this.currentStepIndex+1) {
                this.nextStep = this.selectedExercise.Steps[++this.currentStepIndex];
            }
        }
        this.updateAssembly();
    }

    public dilaogCanceled(event) {
        this.stepDialog.hideDialog();
    }
    
    public dialogPrevious(event) {
        this.stepDialog.hideDialog();
        this.jumpOn(this.currentStepIndex - 1);
    }

    private jumpOn(index: number) {
        this.currentStep = this.stepsSoFar[index];
        this.currentStepIndex = index;
        for (let i = 0; i < this.stepsSoFar.length; i++) {
            if (i <= index)
                this.stepsSoFar[i].underRevision = false;
            else
                this.stepsSoFar[i].underRevision = true;
        }
        this.stepDialog.showDialog(this.currentStep, this.currentStepIndex > 0);
        //this.updateExplanatoryMaterial();
        //this.updateArgumentGraph();
    }

    private updateAssembly() {
        this.blockedGUI = true;
        this.documentUpdated = false;
        this.argumentGraphUpdated = false;
        this.explanatoryMaterialUpdated = false;
        this.assemblyService.putAssembly(this.assemblyId, this.selectedExercise).subscribe((result: string) => {
            this.updateDocument();
            this.updateArgumentGraph();
            this.updateExplanatoryMaterial(true);
        });
    }

    private tryToUnblockGUI() {
        if (this.documentUpdated && this.argumentGraphUpdated && this.explanatoryMaterialUpdated) {
            this.stepDialog.hideDialog();
            if (this.nextStep != null) {
                this.stepsSoFar.push(this.nextStep);
                this.currentStep = this.nextStep;
                this.currentStep.answer = null;
                this.currentStep.underRevision = false;
                this.scrollableProgressPanel.nativeElement.scrollLeft = this.scrollableProgressPanel.nativeElement.scrollWidth + 80;
                this.stepDialog.showDialog(this.currentStep, true);
            }
            this.blockedGUI = false;
        }
    }
    
    private updateDocument() {
        this.assemblyService.getDocument(this.assemblyId).subscribe((result: string) => {
            this.document = this.domSanitizer.bypassSecurityTrustHtml(result);
            this.documentUpdated = true;
            this.tryToUnblockGUI();
        });
    }
     
    private updateExplanatoryMaterial(blockedGUI: boolean) {
        this.currentStepExplanations.length = 0;
        if (this.currentStep != null) {
            for (let stepExplanation of this.currentStep.StepExplanations) {
                this.explanatoryMaterialService.getExplanatoryMaterial(stepExplanation.ref).subscribe(result => {
                    this.currentStepExplanations.push(<ExplanatoryMaterial>result);
                    this.changeDetectorRef.detectChanges();
                });
            }
        }
        if (blockedGUI) {
            this.explanatoryMaterialUpdated = true;
            this.tryToUnblockGUI();
        }
    }
    
    private updateArgumentGraph() {
        if (this.currentStep.ruleFact != null && this.currentStep.ruleFact != "") {
            this.argumentGraphUrl = this.assemblyService.getArgumentGraphUrl(this.assemblyId) + "?_ts=" + new Date().getTime();
        } else {
            this.argumentGraphUpdated = true;
            this.tryToUnblockGUI();
        }
    }
    
    public onLoadArgumentGraph() {
        this.argumentGraphUpdated = true;
        this.tryToUnblockGUI();
    }
    
    public showHelp() {
        this.helpDialog.showDialog();
    }


    ngOnInit() {
        document.getElementById('loader').style.display = "none";
    }
    ngAfterViewInit() {
        this.loginDialog.showDialog();
        this.changeDetectorRef.detectChanges();
    }
    
    panelArgumentGraphToggle(event) {
        if (event.collapsed) {
            document.getElementById("explanatoryDiv").style.height = "calc(100vh - 280px)";
        } else {
            document.getElementById("explanatoryDiv").style.height = "calc(100vh - 540px)";
        }
    }
    
    panelExplanatoryToggle(event) {
        if (event.collapsed) {
            document.getElementById("graphDiv").style.height = "calc(100vh - 280px)";
        } else {
            document.getElementById("graphDiv").style.height = "250px";
        }
    }
    
}
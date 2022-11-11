import {Component, ViewChild, ElementRef, ChangeDetectorRef, Renderer} from '@angular/core';
import {DomSanitizer, SafeResourceUrl, SafeUrl} from '@angular/platform-browser';
import {StepDialogComponent} from "./step-dialog.component";
import {LoginDialogComponent} from "./login-dialog.component";
import {HelpDialogComponent} from "./help-dialog.component";
import {SettingsDialogComponent} from "./settings-dialog.component";
import {Button} from "primeng/primeng";
import {SelectItem} from "primeng/primeng";
import {TooltipModule} from "primeng/primeng";
import {BlockUIModule} from "primeng/primeng";
import {Settings} from './models/settings';
import {Exercise} from './viewmodels/exercise';
import {Step} from './viewmodels/step';
import {ExplanatoryMaterial} from './viewmodels/explanatoryMaterial';
import {ExerciseService} from './services/exercise.service';
import {AssemblyService} from './services/assembly.service';
import {ExplanatoryMaterialService} from './services/explanatoryMaterial.service';
import {TranslateService} from "ng2-translate";

declare var vis: any;

@Component({
    selector: "exercise-component",
    // directives: [Button],
    template: `
 
<login-dialog #loginDialog (onConfirm)="loginConfirmed($event)"></login-dialog>
<button pButton type="text" (click)="closeExercise()" icon="fa-close" style="position: absolute; top: 5px; right: 5px;"></button>
<button pButton type="text" (click)="showHelp()" icon="fa-question" style="position: absolute; top: 5px; right: 45px;"></button>
<button pButton type="text" (click)="showSettings()" icon="fa-cog" style="position: absolute; top: 5px; right: 85px;"></button>
<help-dialog #helpDialog></help-dialog>
<settings-dialog #settingsDialog (onConfirm)="settingsConfirmed($event)"></settings-dialog>
<div>
    <p-panel header="{{'Exercise.ProgressPanelTitle'|translate}} {{selectedExercise!=null ? (' - '+selectedExercise.name) : ''}}" [style]="{'margin':'10px'}">
        <div #scrollableProgressPanel style="overflow-x: scroll; white-space: nowrap; overflow-y: hidden; height: 55px;">
            <div *ngFor="let step of stepsSoFar; let i=index" style="display: inline;">
                <button pButton type="text" id="{{'stepButton-'+i}}" (click)="jumpOn(i)" label="{{'Exercise.StepLabel'|translate}} {{i+1}}" [style.background]="step.underRevision ? 'gray' : '#2399e5'" pTooltip="{{step.text}}" tooltipPosition="top"></button>
                <i *ngIf="i<stepsSoFar.length-1" class="fa fa-arrow-right" aria-hidden="true"></i>
            </div>
        </div>
        <step-dialog #stepDialog (onConfirm)="dilaogConfirmed($event)" (onCancel)="dilaogCanceled($event)" (onPrevious)="dialogPrevious($event)"></step-dialog>
    </p-panel>
<div>
    <div style="float:left; width:50%">
        <p-panel #panelDocument header="{{'Exercise.DocumentPanelTitle'|translate}}" [style]="{'margin':'0 0 10px 10px'}">
            <div style="position: relative; height: 0">
                <div style="position: absolute; top: -37px; right: 50px; text-align: right; font-size: 10pt;">
                    <button pButton type="button" (click)="downloadRTF()" icon="fa-download" iconPos="left" label="RTF"></button>
                    <button pButton type="button" (click)="downloadAKN()" icon="fa-download" iconPos="left" label="AKN"></button>
                    <button pButton type="button" (click)="downloadPDF()" icon="fa-download" iconPos="left" label="PDF"></button>
                </div>
            </div>
            <div [innerHTML]="document" style="height: calc(100vh - 230px); width: 100%; resize: none; overflow: scroll;"></div>
            <!-- textarea pInputTextarea [(ngModel)]="document" style="height: calc(100vh - 230px); width: 100%; resize: none;"></textarea -->
        </p-panel>
    </div>
    <div style="float:right; width:50%;">
        <div style="padding: 0 0 0 15px">
            <p-panel #panelArgumentGraph header="{{'Exercise.ArgumentGraphPanelTitle'|translate}}" [toggleable]="true" (onBeforeToggle)="panelArgumentGraphToggle($event,true)" (onAfterToggle)="panelArgumentGraphToggle($event,false)" [style]="{'margin':'0 10px 10px 0'}">
                <div id="graphDiv" style="height: 250px; overflow: scroll;">
                    <!-- <img *ngIf="argumentGraphUrl != null" [src]='argumentGraphUrl' (load)="onLoadArgumentGraph()"/> -->
                </div>
            </p-panel>
            <p-panel #panelExplanatoryMaterial header="{{'Exercise.ExplanatoryMaterialPanelTitle'|translate}}" [toggleable]="true" (onBeforeToggle)="panelExplanatoryToggle($event,true)" (onAfterToggle)="panelExplanatoryToggle($event,false)" [style]="{'margin':'0 10px 0 0'}">
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
    @ViewChild('settingsDialog')
    settingsDialog: SettingsDialogComponent;
    @ViewChild('stepDialog')
    stepDialog: StepDialogComponent;
    @ViewChild('scrollableProgressPanel')
    scrollableProgressPanel: ElementRef;

    dropdownItems: SelectItem[];
    dropdownSelection: SelectItem;
    exercises: Exercise[];
    selectedExercise: Exercise;
    stepsSoFar: Step[] = new Array<Step>();
    currentStepIndex: number;
    assemblyId: number;
    document: any;
    currentStepExplanations: ExplanatoryMaterial[] = new Array<ExplanatoryMaterial>();
    //argumentGraphUrl: string;
    blockedGUI: boolean = false;
    documentUpdated: boolean = false;
    argumentGraphUpdated: boolean = false;
    explanatoryMaterialUpdated: boolean = false;
    factsAlreadyInferenced: string = ""; // for optimization purposes
    
    settings = new Settings();

    constructor(public exerciseService: ExerciseService,
                public assemblyService: AssemblyService,
                public explanatoryMaterialService: ExplanatoryMaterialService,
                public changeDetectorRef: ChangeDetectorRef,
                private domSanitizer: DomSanitizer,
                private elementRef: ElementRef,
                private renderer: Renderer,
                private translate: TranslateService) {
        this.dropdownItems = [];
        this.dropdownItems.push({ label: 'Select exercise', value: null });

        this.changeDetectorRef = changeDetectorRef;
    }
    
    private currentStep():Step {
        return this.selectedExercise.Steps[this.currentStepIndex];
    }

    public loginConfirmed(event) {
        this.selectedExercise = event.value;
        this.stepsSoFar.length = 0;
        this.clearDocument();
        this.clearArgumentGraph();
        this.clearExplanations();
        if (this.selectedExercise != null) {
            for (let i=0; i<this.selectedExercise.Steps.length; i++) {
                this.selectedExercise.Steps[i].answer = null; // clears answers if same exercise was already answered
            }
            this.assemblyService.postAssembly(this.selectedExercise).subscribe(result => {
                this.assemblyId = parseInt(result.toString());
                this.loginDialog.hideDialog();
                if (this.selectedExercise.Steps != null) {
                    if (this.selectedExercise.Steps.length > 0) {
                        this.currentStepIndex = 0;
                        var currentStep = this.currentStep();
                        this.stepsSoFar.push(currentStep);
                        this.changeDetectorRef.detectChanges();
                        this.stepDialog.showDialog(currentStep, false, document.getElementById("stepButton-" + this.currentStepIndex));
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
        let oldValue: string = this.currentStep().answer;
        let newValue: string = event.value;
        let nextStep: Step = null;
        this.stepDialog.hideDialog();
        if (oldValue == newValue) {  // same answer
            if (this.selectedExercise.Steps.length > this.currentStepIndex+1) {
                this.currentStepIndex++;
                nextStep = this.currentStep();
            }
        } else {  // different answer
            this.currentStep().answer = newValue;
            if (this.settings.revisionClearsFollowingAnswers) {
                this.stepsSoFar.length = this.currentStepIndex + 1; // cuts the rest of steps
                for (let i=this.currentStepIndex+1; i<this.selectedExercise.Steps.length; i++) {
                    this.selectedExercise.Steps[i].answer = null; // clears answers
                }
            }
            if (this.selectedExercise.Steps.length > this.currentStepIndex+1) {
                this.currentStepIndex++;
                nextStep = this.currentStep();
            }
        }
        if (nextStep != null) {
            if (this.stepsSoFar.indexOf(nextStep) == -1) { // steps under revision are already present
                this.stepsSoFar.push(nextStep);
            }
            var currentStep: Step = nextStep;
            currentStep.underRevision = false;
            this.changeDetectorRef.detectChanges();
            this.scrollableProgressPanel.nativeElement.scrollLeft = this.scrollableProgressPanel.nativeElement.scrollWidth + 80;
            this.stepDialog.showDialog(currentStep, true, document.getElementById("stepButton-" + this.currentStepIndex));
        }
        this.updateAssembly();
    }

    public dilaogCanceled(event) {
        this.stepDialog.hideDialog();
    }
    
    public dialogPrevious(event) {
        if (this.currentStepIndex > 0) {
            this.stepDialog.hideDialog();
            this.jumpOn(this.currentStepIndex - 1);
        }
    }

    private jumpOn(index: number) {
        this.currentStepIndex = index;
        if (!this.settings.revisionConsidersFollowingAnswers) {
            for (let i = 0; i < this.stepsSoFar.length; i++) {
                if (i <= index)
                    this.stepsSoFar[i].underRevision = false;
                else
                    this.stepsSoFar[i].underRevision = true;
            }
        }
        this.stepDialog.showDialog(this.currentStep(), this.currentStepIndex > 0, document.getElementById("stepButton-" + this.currentStepIndex));
        this.updateAssembly();
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
            this.blockedGUI = false;
        }
    }
    
    private updateDocument() {
        this.assemblyService.getDocument(this.assemblyId).subscribe((result: string) => {
            this.document = this.domSanitizer.bypassSecurityTrustHtml(result);
            this.documentUpdated = true;
            this.tryToUnblockGUI();
            this.changeDetectorRef.detectChanges();
            for (let i = 0; i < this.stepsSoFar.length; i++) {
                let factName: string = this.stepsSoFar[i].templateFact;
                if (factName != null && factName != "") {
                    if (document.getElementById("fact-" + factName) != null) { // sets click listeners for document fragments
                        this.renderer.listen(document.getElementById("fact-" + factName), 'click', (event) => {
                            this.jumpOn(i);
                        });
                    }
                }
            }
        });
    }
     
    private updateExplanatoryMaterial(blockedGUI: boolean) {
        this.currentStepExplanations.length = 0;
        if (this.currentStep != null) {
            for (let stepExplanation of this.currentStep().StepExplanations) {
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
    
    private updateArgumentGraph() {/*
        if (this.currentStep.ruleFact != null && this.currentStep.ruleFact != "") {
            this.argumentGraphUrl = this.assemblyService.getArgumentGraphUrl(this.assemblyId) + "?_ts=" + new Date().getTime();
        } else {
            this.argumentGraphUpdated = true;
            this.tryToUnblockGUI();
        }*/
        var factsSoFar = "";
        for (let step of this.stepsSoFar) {
            if (step.ruleFact != null && step.ruleFact != "" && !step.underRevision) {
                factsSoFar += step.answer;
            }
        }
        if (factsSoFar != this.factsAlreadyInferenced) {
            this.factsAlreadyInferenced = factsSoFar;
            this.assemblyService.getArgumentGraph(this.assemblyId, this.translate.currentLang).subscribe((result: any) => {
                for (var i in result.nodes) {
                    var label: string = result.nodes[i].label;
                    while (label.indexOf("|") >= 0) {
                        label = label.replace("|","\n");
                    }
                    result.nodes[i].label = label;
                }
                var nodes = new vis.DataSet(result.nodes);
                var edges = new vis.DataSet(result.edges);
                var container = document.getElementById('graphDiv');
                var data = {
                    nodes: nodes,
                    edges: edges
                };
                var options = {
                    layout: {
                        hierarchical: {
                            direction: 'LR',
                            sortMethod: 'directed'
                        }
                    },
                    physics: {
                        enabled: false
                    },
                    interaction: {
                        dragNodes: false
                    }
                };
                var network = new vis.Network(container, data, options);
                let thisRef = this;
                network.on("click", function(params) {
                    if (params.nodes.length == 1) {
                        for (var i in result.nodes) {
                            if (result.nodes[i].id == params.nodes[0]) {
                                if (result.nodes[i].step != null) {
                                    // thisRef instead of this (because this doesn't reference exercise componenet)
                                    thisRef.jumpOn(result.nodes[i].step * 1);  // *1 because step is a string
                                }
                            }
                        }
                    }
                });
                this.argumentGraphUpdated = true;
                this.tryToUnblockGUI();
            });
        } else {
            this.argumentGraphUpdated = true;
            this.tryToUnblockGUI();
        }
    }
    private clearDocument() {
        this.document = "";
    }
    private clearArgumentGraph() {
        // this.argumentGraphUrl = null;
        this.factsAlreadyInferenced = "";
        document.getElementById('graphDiv').innerHTML = "";
    }
    private clearExplanations() {
        this.currentStepExplanations.length = 0;
    }
    
    public onLoadArgumentGraph() {
        this.argumentGraphUpdated = true;
        this.tryToUnblockGUI();
    }
    
    private factClicked(factName: string) {
        for (let i = 0; i < this.stepsSoFar.length; i++) {
            if (this.stepsSoFar[i].templateFact == factName) {
                this.jumpOn(i);
            }
        }
    }
    
    public showHelp() {
        // this.helpDialog.showDialog();
        var win = window.open('app/resources/help.html', '_blank');
        win.focus();
    }
    public showSettings() {
        this.settingsDialog.showDialog(this.settings);
    }
     public settingsConfirmed(event) {
         this.settings = event.value;
         this.settingsDialog.hideDialog();
     }
     public downloadRTF() {
        var url = this.assemblyService.getDocumentRtfUrl(this.assemblyId);
        var win = window.open(url, '_blank');
        win.focus();
     }
     public downloadAKN() {
        var url = this.assemblyService.getDocumentAknUrl(this.assemblyId);
        var win = window.open(url, '_blank');
        win.focus();
     }
     public downloadPDF() {
        var url = this.assemblyService.getDocumentPdfUrl(this.assemblyId);
        var win = window.open(url, '_blank');
        win.focus();
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
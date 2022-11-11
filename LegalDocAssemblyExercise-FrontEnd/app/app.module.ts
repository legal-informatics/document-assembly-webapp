import {NgModule}      from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule}    from '@angular/http';
import {BrowserModule} from '@angular/platform-browser';
import 'rxjs/add/operator/toPromise';
import 'rxjs/Rx';
import {TranslateModule} from 'ng2-translate';

import {MainComponent}  from './main.component';
import {ExerciseComponent}  from './exercise.component';
import {LoginDialogComponent} from "./login-dialog.component"
import {StepDialogComponent} from "./step-dialog.component"
import {HelpDialogComponent} from "./help-dialog.component"
import {SettingsDialogComponent} from "./settings-dialog.component"
import {ExerciseService} from './services/exercise.service';
import {AssemblyService} from './services/assembly.service';
import {ExplanatoryMaterialService} from './services/explanatoryMaterial.service';
import {PanelModule, DropdownModule, InputTextareaModule, TabViewModule, InputTextModule, DataTableModule, ButtonModule, CheckboxModule, DialogModule, MessagesModule, OverlayPanelModule, TooltipModule, BlockUIModule} from 'primeng/primeng';

@NgModule({
  imports: [BrowserModule, FormsModule, HttpModule, TranslateModule.forRoot(), PanelModule, DropdownModule, InputTextareaModule, TabViewModule, InputTextModule, DataTableModule, ButtonModule, CheckboxModule, DialogModule, OverlayPanelModule, MessagesModule, TooltipModule, BlockUIModule],
  declarations: [MainComponent, ExerciseComponent, LoginDialogComponent, HelpDialogComponent, SettingsDialogComponent, StepDialogComponent],
  bootstrap:    [MainComponent],
  providers:    [ExerciseService, AssemblyService, ExplanatoryMaterialService]
})
export class AppModule { }

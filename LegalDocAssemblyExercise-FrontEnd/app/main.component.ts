import {Component} from '@angular/core';
import 'rxjs/add/operator/toPromise';

import {ExerciseComponent} from "./exercise.component"

 
@Component({
    selector: "main-component",
//    directives: [StepsComponent, DocumentComponent],
    template: `
        <div style="text-align:center;">
            <h1 style="margin: 5px;">Legal Document Assembly Exercise System</h1>
        </div>
        <exercise-component>...</exercise-component>
        `,
        styles: [`
        `]
})
export class MainComponent {
    
}
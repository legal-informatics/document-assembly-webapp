import {Injectable} from "@angular/core";
import {Http, Response} from '@angular/http';
import {Exercise} from "../viewmodels/exercise";
import {Observable} from "rxjs/Observable";

@Injectable()
export class ExerciseService {
    constructor(public http:Http) {
        
    }
    
    private exerciseBaseUrl = 'http://localhost:8080/LegalDocAssemblyExercise/rest/exercises/';
    // private exerciseBaseUrl = './app/services/test_exercises.json';
    // private exerciseBaseUrl = 'rest/exercises/';
    
    getExercises() {
        return this.http.get(this.exerciseBaseUrl)
            .map(response => <Exercise[]>response.json())
            .catch(this.handleError);
    }
    getExercise(name: string) {
        return this.http.get(this.exerciseBaseUrl + name)
            .map(response => <Exercise>response.json())
            .catch(this.handleError);
    }
    
    private handleError(error: Response) {
        console.error(error);
        return Observable.throw(error.json().error || "Server error");
    }
}
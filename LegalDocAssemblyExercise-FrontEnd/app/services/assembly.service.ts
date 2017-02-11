import {Injectable} from "@angular/core";
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import {Exercise} from "../viewmodels/exercise";
import {Observable} from "rxjs/Observable";

@Injectable()
export class AssemblyService {
    
    constructor(public http:Http) {
        
    }
    
    private assemblyBaseUrl = 'http://localhost:8080/LegalDocAssemblyExercise/rest/assembly/';
    // private assemblyBaseUrl = 'rest/assembly/';
    
    postAssembly(exercise: Exercise) {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        return this.http.post(this.assemblyBaseUrl, exercise, options)
            .map(response => response.text())
            .catch(this.handleError);
    }
    putAssembly(assemblyId: number, exercise: Exercise) {
        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        return this.http.put(this.assemblyBaseUrl + assemblyId, exercise, options)
            .map(response => response.text())
            .catch(this.handleError);
    }
    getDocument(assemblyId: number) {
        return this.http.get(this.assemblyBaseUrl + assemblyId + "/indictment.xml")
            .map(response => <String>response.text())
            .catch(this.handleError);
    }
    
    private handleError(error: Response) {
        console.error(error);
        return Observable.throw(error.json().error || "Server error");
    }
    
    public getArgumentGraphUrl(assemblyId: number): string {
        return this.assemblyBaseUrl + assemblyId + "/indictment.png";
    }
    
}
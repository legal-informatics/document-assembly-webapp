import {Injectable} from "@angular/core";
import {Http, Response} from '@angular/http';
import {ExplanatoryMaterial} from "../viewmodels/explanatoryMaterial";
import {Observable} from "rxjs/Observable";

@Injectable()
export class ExplanatoryMaterialService {
    constructor(public http:Http) {
        
    }
    
    private explanatoryBaseUrl = 'http://localhost:8080/LegalDocAssemblyExercise/rest/explanatoryMaterial/';
    // private explanatoryBaseUrl = 'rest/explanatoryMaterial/';
    
    getExplanatoryMaterials() {
        return this.http.get(this.explanatoryBaseUrl)
            .map(response => <ExplanatoryMaterial[]>response.json())
            .catch(this.handleError);
    }
    getExplanatoryMaterial(id: string) {
        return this.http.get(this.explanatoryBaseUrl + id)
            .map(response => <ExplanatoryMaterial>response.json())
            .catch(this.handleError);
    }
    
    private handleError(error: Response) {
        console.error(error);
        return Observable.throw(error.json().error || "Server error");
    }
}
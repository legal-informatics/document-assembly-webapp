import {StepExplanation} from './stepExplanation';

export class Step {
    public text: string;
    public ruleFact: string;
    public templateFact: string;

    public answer: string;
    public answerType: string;
    
    public StepExplanations: StepExplanation[];
    
    public underRevision: boolean = false;
    
    constructor(
    ) { }
}

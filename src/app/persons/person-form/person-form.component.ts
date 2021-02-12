import {Component, OnInit} from '@angular/core';
import {Launch, Person} from '../../core/model';
import {ErrorHandlerService} from '../../core/error-handler.service';
import {PersonService} from '../person.service';
import {ToastyService} from 'ng2-toasty';
import {NgForm} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Title} from '@angular/platform-browser';

@Component({
  selector: 'app-person-form',
  templateUrl: './person-form.component.html',
  styleUrls: ['./person-form.component.css']
})
export class PersonFormComponent implements OnInit {

  person = new Person();

  constructor(private errorHandlerService: ErrorHandlerService,
              private personService: PersonService,
              private toastyService: ToastyService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private title: Title) {
  }

  ngOnInit() {
    this.title.setTitle('Nova pessoa');

    const personId = this.activatedRoute.snapshot.params.id;

    if (personId) {
      this.loadPerson(personId);
    }
  }

  get isEdit() {
    return Boolean(this.person.id);
  }

  save(launchForm: NgForm) {
    if (this.isEdit) {
      this.update(launchForm);
    } else {
      this.add(launchForm);
    }
  }

  update(personForm: NgForm) {
    this.personService.update(this.person)
      .subscribe(personUpdated => {
          this.person = personUpdated;
          this.updateEditTitle();
          this.toastyService.success('Pessoa atualizada com sucesso!');
        },
        error => this.errorHandlerService.handle(error)
      );
  }

  add(personForm: NgForm) {
    this.personService.save(this.person)
      .subscribe(() => {
          this.toastyService.success('Pessoa adicionanda com sucesso!');
          personForm.reset();
          this.person = new Person();
        },
        error => this.errorHandlerService.handle(error)
      );
  }

  new(personForm: NgForm) {
    personForm.reset();
    this.router.navigate(['/persons/new']);
  }

  private loadPerson(id: number) {
    this.personService.findById(id)
      .subscribe(person => {
          this.person = person;
          this.updateEditTitle();
        },
        error => this.errorHandlerService.handle(error));
  }

  private updateEditTitle() {
    this.title.setTitle(`Edição da pessoa de nome: ${this.person.name}`);
  }
}
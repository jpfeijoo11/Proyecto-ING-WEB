import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-crud',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './crud.html', 
  styleUrl: './crud.css'
})
export class CrudComponent implements OnInit {
  productos: any[] = [];
  nuevo = { nombre: '', url: '', precioObjetivo: 0 };

  constructor(private http: HttpClient) {}

  ngOnInit() { this.listar(); }

  listar() {
    this.http.get<any[]>('http://localhost:8080/api/productos').subscribe(data => this.productos = data);
  }

  guardar() {
    this.http.post('http://localhost:8080/api/productos', this.nuevo).subscribe(() => {
      this.listar();
      this.nuevo = { nombre: '', url: '', precioObjetivo: 0 };
    });
  }

  borrar(id: number) {
    this.http.delete(`http://localhost:8080/api/productos/${id}`).subscribe(() => this.listar());
  }
}
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AduanaService,
  OperacionAduanera,
  DetalleRiesgo,
  ImportadorHistorial,
  RestriccionArancelaria,
  CatalogoRiesgoPais
} from '../../services/aduana.service';

@Component({
  selector: 'app-operaciones',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './operaciones.html',
  styleUrls: ['./operaciones.css']
})
export class OperacionesComponent implements OnInit {

  operaciones: OperacionAduanera[] = [];
  operacionForm: FormGroup;
  isSubmitting = false;

  // Catálogos para los selects del formulario
  importadores: ImportadorHistorial[] = [];
  arancelarios: RestriccionArancelaria[] = [];
  puertosDisponibles: CatalogoRiesgoPais[] = [];
  fasesDisponibles: string[] = []; // ← preestablecidos desde BD

  // Control de permisos por rol
  rolActual: string = '';
  isAdmin: boolean = false;
  isInspector: boolean = false;
  isAgente: boolean = false;

  // Panel de análisis de riesgo (se abre al registrar O al hacer click en un registro)
  ultimaOperacion: OperacionAduanera | null = null;
  ultimoDetalle: DetalleRiesgo | null = null;
  cargandoAnalisis: boolean = false;
  operacionSeleccionadaId: number | null = null;

  constructor(
    private aduanaService: AduanaService,
    private fb: FormBuilder
  ) {
    this.operacionForm = this.fb.group({
      numeroTracking:    ['', [Validators.required, Validators.pattern(/^[A-Z0-9-]+$/)]],
      tipoOperacion:     ['', Validators.required],
      puertoOrigen:      ['', Validators.required],   // ← ahora viene del select de puertos
      puertoDestino:     ['', Validators.required],
      idImportador:      [null],
      codigoArancelario: ['']
    });
  }

  ngOnInit(): void {
    this.determinarPermisos();
    this.cargarOperaciones();
    this.cargarCatalogos();  // todos los roles necesitan los catálogos para el dropdown
  }

  determinarPermisos(): void {
    const usuarioJSON = localStorage.getItem('usuarioActual');
    if (usuarioJSON) {
      const usuario = JSON.parse(usuarioJSON);
      this.rolActual   = usuario.rol?.toUpperCase() || '';
      this.isAdmin     = this.rolActual === 'ADMINISTRADOR' || this.rolActual === 'ADMIN';
      this.isInspector = this.rolActual === 'INSPECTOR';
      this.isAgente    = this.rolActual === 'AGENTE';
    }
  }

  cargarOperaciones(): void {
    this.aduanaService.getOperaciones().subscribe({
      next: (data) => this.operaciones = data,
      error: (err) => console.error('Error al cargar operaciones', err)
    });
  }

  cargarCatalogos(): void {
    // Puertos preestablecidos (Ecuador, España, USA) con nivel de riesgo
    this.aduanaService.getPaisesRiesgo().subscribe({
      next: (data) => this.puertosDisponibles = data,
      error: (err) => console.error('Error al cargar puertos', err)
    });

    if (this.isAdmin) {
      this.aduanaService.getImportadores().subscribe({
        next: (data) => this.importadores = data,
        error: (err) => console.error('Error al cargar importadores', err)
      });
      this.aduanaService.getArancelarios().subscribe({
        next: (data) => this.arancelarios = data,
        error: (err) => console.error('Error al cargar arancelarios', err)
      });
    }
  }

  onSubmit(): void {
    if (this.operacionForm.invalid) return;

    this.isSubmitting = true;
    this.cerrarAnalisis();

    const formValue = this.operacionForm.value;
    const nuevaOperacion: OperacionAduanera = {
      numeroTracking:    formValue.numeroTracking,
      tipoOperacion:     formValue.tipoOperacion,
      puertoOrigen:      formValue.puertoOrigen,
      puertoDestino:     formValue.puertoDestino,
      idImportador:      formValue.idImportador ? Number(formValue.idImportador) : undefined,
      codigoArancelario: formValue.codigoArancelario || undefined,
      estado:            'DOCUMENTACION'
    };

    this.aduanaService.crearOperacion(nuevaOperacion).subscribe({
      next: (response) => {
        this.operaciones.unshift(response.operacion);
        this.mostrarAnalisis(response.operacion, response.detalleRiesgo);
        this.operacionForm.reset();
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Error al crear operación', err);
        this.isSubmitting = false;
      }
    });
  }

  // ── Ver análisis de una operación EXISTENTE ──────────────────────────────

  verAnalisis(operacion: OperacionAduanera): void {
    if (!operacion.id) return;

    // Si ya está mostrando esta misma operación, cerrar (toggle)
    if (this.operacionSeleccionadaId === operacion.id) {
      this.cerrarAnalisis();
      return;
    }

    this.cargandoAnalisis = true;
    this.operacionSeleccionadaId = operacion.id;
    this.ultimaOperacion = null;
    this.ultimoDetalle   = null;

    this.aduanaService.getAnalisisOperacion(operacion.id).subscribe({
      next: (response) => {
        this.mostrarAnalisis(response.operacion, response.detalleRiesgo);
        this.cargandoAnalisis = false;
      },
      error: (err) => {
        console.error('Error al cargar análisis', err);
        this.cargandoAnalisis = false;
      }
    });
  }

  private mostrarAnalisis(op: OperacionAduanera, detalle: DetalleRiesgo): void {
    this.ultimaOperacion = op;
    this.ultimoDetalle   = detalle;
    this.operacionSeleccionadaId = op.id ?? null;
    // Scroll suave al panel de análisis
    setTimeout(() => {
      document.getElementById('panel-analisis')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
  }

  cerrarAnalisis(): void {
    this.ultimaOperacion = null;
    this.ultimoDetalle   = null;
    this.operacionSeleccionadaId = null;
  }





  // ── Máquina de estados ──────────────── ───────────────────────────────────

  avanzarFlujo(operacion: OperacionAduanera): void {
    this.aduanaService.actualizarEstadoOperacion(operacion.id!, this.getSiguienteEstado(operacion.estado)).subscribe({
      next: (opActualizada) => {
        // Actualizar estado localmente sin recargar toda la lista
        const index = this.operaciones.findIndex(op => op.id === opActualizada.id);
        if (index !== -1) {
          this.operaciones[index] = opActualizada;
          // Si se está viendo el análisis de esta operación, actualizarlo también
          if (this.operacionSeleccionadaId === opActualizada.id && this.ultimoDetalle) {
            this.ultimoDetalle.canalAforo = opActualizada.canalAforo || this.ultimoDetalle.canalAforo;
            this.ultimoDetalle.descripcionCanal = this.getLabelCanal(this.ultimoDetalle.canalAforo);
          }
        }
      },
      error: (err) => console.error('Error al avanzar flujo', err)
    });
  }


  getSiguienteEstado(estado: string): string {
    switch (estado) {
      case 'DOCUMENTACION': return 'AFORO';
      case 'AFORO':         return 'FINALIZADA';
      default:             return estado; // no cambia
    }
  }

  subirDocumento(operacion: OperacionAduanera): void {
    alert(`📎 Carga de documentos: ${operacion.numeroTracking}\n(Funcionalidad próximamente)`);
  }

  // ── Eliminar operación (solo Admin) ──────────────────────────────────────

  eliminarOperacion(operacion: OperacionAduanera): void {
    if (!operacion.id) return;

    const confirmar = confirm(
      `⚠️ ¿Eliminar la operación "${operacion.numeroTracking}"?\n\nEsta acción es permanente y no se puede deshacer.`
    );
    if (!confirmar) return;

    this.aduanaService.eliminarOperacion(operacion.id).subscribe({
      next: () => {
        // Actualizar lista local inmediatamente sin recargar
        this.operaciones = this.operaciones.filter(op => op.id !== operacion.id);
        // Si se estaba viendo el análisis de esa operación, cerrarlo
        if (this.operacionSeleccionadaId === operacion.id) {
          this.cerrarAnalisis();
        }
      },
      error: (err) => console.error('Error al eliminar operación', err)
    });
  }

  // ── Helpers visuales ─────────────────────────────────────────────────────

  getEmojiCanal(canal: string | undefined): string {
    switch (canal) {
      case 'VERDE':    return '🟢';
      case 'AMARILLO': return '🟡';
      case 'ROJO':     return '🔴';
      default:         return '⚪';
    }
  }

  getLabelCanal(canal: string | undefined): string {
    switch (canal) {
      case 'VERDE':    return 'Verde — Automático';
      case 'AMARILLO': return 'Amarillo — Documental';
      case 'ROJO':     return 'Rojo — Físico';
      default:         return 'Sin evaluar';
    }
  }

  /** Etiqueta breve del nivel de riesgo para mostrar junto al puerto en el select */
  getPuertoLabel(puerto: CatalogoRiesgoPais): string {
    const emoji = puerto.nivelRiesgo === 'ALTO' ? '🔴' :
                  puerto.nivelRiesgo === 'MEDIO' ? '🟡' : '🟢';
    return `${emoji} ${puerto.nombrePuertoOPais}`;
  }

  isFieldInvalid(field: string): boolean {
    const control = this.operacionForm.get(field);
    return control ? control.invalid && (control.dirty || control.touched) : false;
  }
}

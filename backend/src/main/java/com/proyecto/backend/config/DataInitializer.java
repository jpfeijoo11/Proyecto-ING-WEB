package com.proyecto.backend.config;

import com.proyecto.backend.models.CatalogoRiesgoPais;
import com.proyecto.backend.models.ImportadorHistorial;
import com.proyecto.backend.models.RestriccionArancelaria;
import com.proyecto.backend.repositories.CatalogoRiesgoPaisRepository;
import com.proyecto.backend.repositories.ImportadorHistorialRepository;
import com.proyecto.backend.repositories.RestriccionArancelariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra los datos de catálogo al iniciar la aplicación.
 *
 * catalogo_riesgo_pais → siempre se reemplaza (datos de configuración del sistema)
 * importador_historial y restricciones_arancelarias → solo si están vacíos
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private CatalogoRiesgoPaisRepository catalogoPaisRepo;
    @Autowired private ImportadorHistorialRepository importadorRepo;
    @Autowired private RestriccionArancelariaRepository restriccionRepo;

    @Override
    public void run(String... args) {
        cargarPuertos();
        cargarImportadores();
        cargarArancelarios();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CATÁLOGO DE PUERTOS — Ecuador · España · USA
    // Se limpia y recarga en cada inicio para mantener el catálogo actualizado.
    // ─────────────────────────────────────────────────────────────────────────
    private void cargarPuertos() {
        catalogoPaisRepo.deleteAll();

        // ── ECUADOR ──────────────────────────────────────────────────────────
        // Guayaquil: mayor puerto del país, alta actividad de narcotráfico
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Guayaquil - Ecuador", "ALTO", 30,
            "Principal puerto del Ecuador. Alta incidencia de tráfico de estupefacientes y contrabando de mercancías."));

        // Manta: ruta activa de precursores químicos
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Manta - Ecuador", "ALTO", 30,
            "Puerto pesquero con ruta activa de precursores químicos y conexiones con redes internacionales."));

        // Esmeraldas: zona fronteriza de vigilancia
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Esmeraldas - Ecuador", "MEDIO", 15,
            "Puerto fronterizo con Colombia. Vigilancia por irregularidades en declaraciones de valor."));

        // Puerto Bolívar: exportación de banano, bajo riesgo
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Puerto Bolívar - Ecuador", "BAJO", 0,
            "Puerto especializado en exportación de productos agrícolas. Bajo índice de incidencias."));

        // ── ESPAÑA ───────────────────────────────────────────────────────────
        // Algeciras: mayor punto de entrada de narcóticos a Europa
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Algeciras - España", "ALTO", 30,
            "Principal puerto de entrada de narcóticos a Europa desde el norte de África. Alto índice de decomisos."));

        // Valencia: vigilancia por subfacturación en textiles y electrónica
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Valencia - España", "MEDIO", 15,
            "Puerto con casos documentados de subfacturación en importaciones de textiles y productos electrónicos."));

        // Barcelona: puerto de carga general, bajo riesgo
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Barcelona - España", "BAJO", 0,
            "Puerto de carga general con alto nivel de fiscalización. Bajo índice de incidencias aduaneras."));

        // Bilbao: puerto industrial, bajo riesgo
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Bilbao - España", "BAJO", 0,
            "Puerto industrial especializado en maquinaria pesada y acero. Bajo riesgo certificado."));

        // ── USA ──────────────────────────────────────────────────────────────
        // Miami: lavado de activos y contrabando de lujo
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Miami - USA", "MEDIO", 15,
            "Centro de operaciones de lavado de activos y contrabando de artículos de lujo hacia América Latina."));

        // Nueva Orleans: ruta histórica de mercancías irregulares
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Nueva Orleans - USA", "MEDIO", 15,
            "Puerto con historial de irregularidades en declaraciones de origen y subfacturación de mercancías."));

        // Los Ángeles: puerto de mayor volumen, bajo riesgo relativo
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Los Ángeles - USA", "BAJO", 0,
            "Mayor puerto en volumen de contenedores de USA. Alta tecnología de escaneo. Bajo riesgo relativo."));

        // Houston: vigilancia por precursores químicos
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Houston - USA", "MEDIO", 15,
            "Puerto petroquímico con vigilancia especial por tráfico de precursores químicos y materiales peligrosos."));

        // Nueva York: bajo riesgo certificado
        catalogoPaisRepo.save(new CatalogoRiesgoPais(
            "Nueva York - USA", "BAJO", 0,
            "Puerto con alto nivel de tecnología de inspección. Bajo índice de incidencias documentadas."));

        System.out.println("[DataInitializer] ✅ Catálogo de puertos cargado: Ecuador (4), España (4), USA (5).");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IMPORTADORES — solo si la tabla está vacía
    // ─────────────────────────────────────────────────────────────────────────
    private void cargarImportadores() {
        if (importadorRepo.count() > 0) return;

        importadorRepo.save(new ImportadorHistorial("Importaciones La Confianza S.A.",    "0992345678001", 0, "Ecuador"));
        importadorRepo.save(new ImportadorHistorial("Comercializadora del Pacífico Cía.", "0993456789001", 1, "Ecuador"));
        importadorRepo.save(new ImportadorHistorial("GlobalTrade Corp.",                   "0994567890001", 3, "Panamá"));   // ⚠️ Infractor
        importadorRepo.save(new ImportadorHistorial("Andina Logistics S.A.",              "0995678901001", 5, "Colombia")); // ⚠️ Infractor
        importadorRepo.save(new ImportadorHistorial("Empresa Modelo S.A.",                "0996789012001", 0, "Ecuador"));
        importadorRepo.save(new ImportadorHistorial("FastImport Solutions",               "0997890123001", 2, "Perú"));
        importadorRepo.save(new ImportadorHistorial("Iberia Cargo S.L.",                  "ES-B12345678",  1, "España"));
        importadorRepo.save(new ImportadorHistorial("Miami Freight Inc.",                 "US-FL-99012",   0, "USA"));

        System.out.println("[DataInitializer] ✅ Importadores con historial cargados.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESTRICCIONES ARANCELARIAS — solo si la tabla está vacía
    // ─────────────────────────────────────────────────────────────────────────
    private void cargarArancelarios() {
        if (restriccionRepo.count() > 0) return;

        restriccionRepo.save(new RestriccionArancelaria("9301.10", "Armas militares y de guerra",         true,  "ARMAMENTO"));
        restriccionRepo.save(new RestriccionArancelaria("9301.90", "Partes y accesorios de armas",        true,  "ARMAMENTO"));
        restriccionRepo.save(new RestriccionArancelaria("2933.59", "Precursores químicos tipo A",         true,  "QUIMICOS"));
        restriccionRepo.save(new RestriccionArancelaria("2921.19", "Compuestos amínicos - precursores",   true,  "QUIMICOS"));
        restriccionRepo.save(new RestriccionArancelaria("3004.90", "Medicamentos de uso controlado",      true,  "MEDICAMENTOS"));
        restriccionRepo.save(new RestriccionArancelaria("2844.40", "Material radiactivo",                 true,  "NUCLEARES"));
        restriccionRepo.save(new RestriccionArancelaria("0201.10", "Carne de res refrigerada",            false, "ALIMENTOS"));
        restriccionRepo.save(new RestriccionArancelaria("8471.30", "Computadoras portátiles",             false, "ELECTRONICA"));
        restriccionRepo.save(new RestriccionArancelaria("6203.42", "Prendas de vestir - algodón",         false, "TEXTILES"));
        restriccionRepo.save(new RestriccionArancelaria("8703.23", "Vehículos 1500-3000cc",               false, "AUTOMOTRIZ"));
        restriccionRepo.save(new RestriccionArancelaria("7108.12", "Oro en polvo sin elaborar",           true,  "METALES_PRECIOSOS"));
        restriccionRepo.save(new RestriccionArancelaria("2710.12", "Gasolina y combustibles derivados",   false, "COMBUSTIBLES"));

        System.out.println("[DataInitializer] ✅ Restricciones arancelarias cargadas.");
    }
}

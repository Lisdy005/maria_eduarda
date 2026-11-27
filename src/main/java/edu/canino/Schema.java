package edu.canino;

import java.util.Arrays;
import weka.core.Instances;

/** Contrato único para generador, ARFF, entrenamiento e interfaz. */
public final class Schema {
    private Schema() { }
    public static final String[] SYMPTOMS = {"fiebre", "vomito", "diarrea", "tos", "letargo",
        "perdida_apetito", "secrecion_oculonasal", "signos_neurologicos", "salivacion_excesiva",
        "prurito", "molestia_oido", "perdida_peso", "sangre_heces", "ictericia", "sed_aumentada"};
    public static final String[] LABELS = {"Fiebre", "Vómito", "Diarrea", "Tos", "Letargo",
        "Pérdida de apetito", "Secreción ocular o nasal", "Signos neurológicos",
        "Salivación excesiva", "Prurito (picazón)", "Molestia en oído",
        "Pérdida de peso", "Sangre en heces", "Ictericia (color amarillo)", "Sed aumentada"};
    public static final String[] DISEASES = {"parvovirosis", "moquillo", "rabia", "otitis",
        "parasitosis_intestinal", "traqueobronquitis_infecciosa", "dermatitis_alergica", "leptospirosis"};
    /** Nombres legibles de las clases, en el mismo orden que {@link #DISEASES}. */
    public static final String[] DISEASE_LABELS = {"Parvovirosis", "Moquillo", "Rabia", "Otitis",
        "Parasitosis intestinal", "Traqueobronquitis infecciosa", "Dermatitis alérgica", "Leptospirosis"};
    public static final String WARNING = "DATOS SINTÉTICOS — SIN VALIDACIÓN CLÍNICA. No sustituye al veterinario.";
    public static final String URGENT = "Ante signos neurológicos, sospecha de rabia/exposición, dificultad respiratoria, "
        + "sangrado, ictericia o deterioro: atención veterinaria urgente, independientemente del resultado. "
        + "Si sospecha rabia, evite contacto con saliva y mordeduras y contacte al veterinario/autoridad sanitaria.";
    public static void validateHeader(Instances data) {
        if (data.numAttributes() != SYMPTOMS.length + 1) throw new IllegalArgumentException("Se requieren 15 síntomas y una clase.");
        for (int i = 0; i < SYMPTOMS.length; i++) {
            if (!data.attribute(i).name().equals(SYMPTOMS[i]) || !data.attribute(i).isNominal()
                || data.attribute(i).numValues() != 2 || !data.attribute(i).value(0).equals("0")
                || !data.attribute(i).value(1).equals("1")) {
                throw new IllegalArgumentException("Atributo incompatible en posición " + (i + 1) + ": " + SYMPTOMS[i] + " {0,1}");
            }
        }
        data.setClassIndex(SYMPTOMS.length);
        if (!data.classAttribute().name().equals("enfermedad") || !data.classAttribute().isNominal()
            || data.numClasses() != DISEASES.length) throw new IllegalArgumentException("Clase nominal incompatible.");
        for (int i = 0; i < DISEASES.length; i++) if (!data.classAttribute().value(i).equals(DISEASES[i]))
            throw new IllegalArgumentException("Orden de clases incompatible: " + Arrays.toString(DISEASES));
    }
}

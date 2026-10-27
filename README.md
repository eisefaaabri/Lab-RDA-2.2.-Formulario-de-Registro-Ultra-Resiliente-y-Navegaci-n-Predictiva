# Laboratorio 2.2: Formulario de Registro Ultra-Resiliente y Navegación Predictiva (API 36)

**Asignatura:** Redes Digitales y Aplicaciones Móviles 1 (RDA1)  
**Grupo de Desarrollo:**
* Joan Santacruz
* Ariel Chacha
* Esteban Garcia
* Anthony Rosero

---

## 📌 Descripción del Proyecto

Aplicación Android desarrollada en **Jetpack Compose** bajo la arquitectura **MVVM (Model-View-ViewModel)** y **Android 16 (API 36)**. El objetivo es garantizar la integridad del estado y prevenir la pérdida de datos del usuario mediante una estrategia de persistencia multinivel:
1. **Memoria RAM (ViewModel):** Retención reactiva durante la recomposición y cambios de configuración.
2. **Estado del Proceso (SavedStateHandle):** Persistencia ante la terminación forzosa de la actividad por el *Low Memory Killer* (LMK) o rotaciones.
3. **Almacenamiento en Disco (DataStore Preferences):** Persistencia permanente no volátil que sobrevive al cierre total de la aplicación (*Swipe Up*) y reinicios del dispositivo.
4. **Navegación Predictiva (Predictive Back):** Interceptación de gestos de regreso mediante `BackHandler` cuando existen datos sin guardar.

---

## ⚖️ Comparativa Técnica: `SavedStateHandle` vs `DataStore Preferences`

| Característica | `SavedStateHandle` | `DataStore Preferences` |
| :--- | :--- | :--- |
| **Ubicación del Almacenamiento** | Memoria del sistema operativo (`OS Bundle` administrado por `system_server` vía IPC/Binder). | Disco local de almacenamiento no volátil (`/data/data/<package>/files/datastore/`). |
| **Ciclo de Vida** | Vinculado a la tarea de navegación en la sesión del usuario. | Independiente del ciclo de vida; permanente hasta desinstalar la app o limpiar datos. |
| **Supervivencia a Rotación** | **Sí** | **Sí** |
| **Supervivencia a Muerte de Proceso (LMK)** | **Sí** (el SO reinyecta el Bundle al recrear la actividad). | **Sí** |
| **Supervivencia a Cierre Total (*Swipe Up*)** | **No** (el registro de la pila de tareas se destruye). | **Sí** (permanece intacto en el disco). |
| **Supervivencia a Reinicio del Dispositivo** | **No** | **Sí** |
| **Límite de Tamaño Recomendado** | **< 1 MB** (estricto para evitar `TransactionTooLargeException`). | Prácticamente ilimitado (orientado a pares clave-valor). |
| **Mecanismo de Acceso** | Síncrono a través del Bundle. | **Asíncrono** mediante Kotlin Coroutines y reactivo con `Flow`. |
| **Caso de Uso Recomendado** | Estado temporal de la UI: texto en edición, índice de scroll, pestañas activas, filtros de búsqueda. | Datos persistentes: configuración, usuario autenticado, borradores permanentes, tokens. |

---

## 🚀 Requisitos y Configuración

* **Android Studio:** Ladybug (2024.2.1+)
* **JDK:** Java 21
* **compileSdk / targetSdk:** 36 (Android 16 - VanillaIceCream)
* **minSdk:** 26
* **Dispositivo de prueba recomendado:** Emulador Google Pixel 8 con API 36 y "Don't keep activities" habilitado para pruebas de LMK.

---

## 📂 Estructura del Código

```text
app/src/main/java/com/example/myapplication/
│
├── data/
│   └── UserPreferences.kt       # Repositorio de persistencia en disco con DataStore Preferences
│
├── ui/
│   ├── FormViewModel.kt         # ViewModel con SavedStateHandle y disparo de guardado en DataStore
│   └── theme/                   # Configuración de tema Material 3
│
└── MainActivity.kt              # UI Compose con BackHandler (Predictive Back) y campos de texto
```

---

## 🧪 Pruebas de Validación Realizadas

1. **Rotación de Pantalla (Ctrl + F12):** Ambos campos (Nombre y Email) preservan su valor.
2. **Process Death (Don't keep activities activado):** Al minimizar y volver, el campo Email (SavedStateHandle) y Nombre (DataStore) se restauran intactos.
3. **Cierre Total (Swipe Up en recientes):** Al abrir la app en frío, el campo Nombre se restaura desde DataStore.
4. **Predictive Back (API 36):** El gesto de atrás con datos en el formulario es interceptado y registrado en Logcat bajo la etiqueta `NAV`.
5. **Feedback Visual:** Al guardar en DataStore se activa un icono de confirmación visible durante 1 segundo.

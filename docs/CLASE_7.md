# Clase 7: Patrones de diseño, testing unitario y TDA lineales

Esta guía usa el proyecto de proformas construido hasta la semana 6. Los
ejemplos Java están en
[`EjemplosClase7.java`](../src/main/java/com/univ2026/proformas/clase7/EjemplosClase7.java)
y sus pruebas en
[`EjemplosClase7Test.java`](../src/test/java/com/univ2026/proformas/clase7/EjemplosClase7Test.java).

## Objetivos

Al finalizar la práctica se podrá:

- distinguir un TDA de su implementación concreta;
- implementar una pila LIFO y una cola FIFO sobre arreglos;
- explicar por qué la cola usa un arreglo circular;
- aplicar Repository para ocultar la estructura usada para almacenar objetos;
- escribir y ejecutar pruebas unitarias con JUnit 5;
- reconocer las técnicas de testing usadas en la semana 6.

## 1. TDA e implementación

Un tipo de dato abstracto se define por su comportamiento observable. Una pila
permite apilar y desapilar siguiendo LIFO; esa definición no obliga a usar un
arreglo, una lista enlazada ni una clase de la biblioteca estándar.

En `EjemplosClase7`, el arreglo es la implementación elegida. Es privado para
que quien usa el TDA dependa de sus operaciones y no pueda modificar
accidentalmente su almacenamiento interno.

La **capacidad** es el tamaño físico del arreglo. La **cantidad** indica cuántos
productos contiene actualmente la estructura. Confundir estos valores causa
desbordamientos o lecturas de posiciones vacías.

## 2. Pila de productos: LIFO

`PilaProductos` mantiene un arreglo y el contador `cantidad`:

```java
elementos[cantidad] = producto;
cantidad++;
```

Para desapilar, primero disminuye el contador. La nueva posición de `cantidad`
es precisamente la que contiene el último elemento agregado:

```java
cantidad--;
Producto tope = elementos[cantidad];
elementos[cantidad] = null;
return tope;
```

Asignar `null` elimina la referencia que ya no pertenece a la pila. La clase
lanza `IllegalStateException` si está llena o vacía; devolver `null` ocultaría
un uso incorrecto y trasladaría el error a otra parte del programa.

Ejemplo conceptual:

```text
apilar(mouse), apilar(teclado)
desapilar() -> teclado
desapilar() -> mouse
```

## 3. Cola circular de productos: FIFO

`ColaProductos` agrega al final y retira desde `frente`. La posición de entrada
se calcula así:

```java
int posicionFinal = (frente + cantidad) % elementos.length;
```

El módulo `%` hace que el índice vuelva a cero al llegar al final del arreglo.
Esto permite reutilizar las posiciones liberadas sin desplazar todos los
elementos. Al desencolar se limpia la posición y se avanza el frente:

```java
Producto primero = elementos[frente];
elementos[frente] = null;
frente = (frente + 1) % elementos.length;
cantidad--;
```

Ejemplo conceptual:

```text
encolar(mouse), encolar(teclado)
desencolar() -> mouse
desencolar() -> teclado
```

El caso más importante para probar es la vuelta del arreglo: desencolar un
elemento, encolar otro y verificar que el nuevo elemento use el espacio libre
al inicio sin alterar el orden FIFO.

## 4. Patrón Repository

`RepositorioProductos` ofrece `agregar`, `retirarUltimo`, `estaVacio` y
`cantidad`. Internamente delega en una `PilaProductos`, pero no expone ni el
arreglo ni las operaciones técnicas `apilar` y `desapilar`.

```text
Código cliente -> RepositorioProductos -> PilaProductos -> Producto[]
```

Esta separación permite expresar el acceso en términos del dominio y mantener
el almacenamiento encapsulado. El repositorio no calcula precios, impuestos o
descuentos: esas son reglas de negocio y no responsabilidades de acceso a
datos.

El proyecto de semana 6 ya contiene ejemplos de este patrón a mayor escala:
`RepositorioProformasSQLite` y `RepositorioCuentasPagoSQLite` ocultan SQL y
SQLite detrás de operaciones del dominio. El nuevo repositorio didáctico hace
lo mismo, aunque su almacenamiento es una pila en memoria.

## 5. Ejecutar los ejemplos

Compilar y ejecutar el archivo demostrativo:

```bash
./mvnw compile exec:java \
  -Dexec.mainClass=com.univ2026.proformas.clase7.EjemplosClase7
```

La salida permite comparar los órdenes:

```text
Pila (LIFO): Teclado
Cola (FIFO): Mouse
Repository: Mouse
```

## 6. Pruebas de la clase 7

Ejecutar solamente las pruebas de estos ejemplos:

```bash
./mvnw -Dtest=EjemplosClase7Test test
```

### Preparación con `@BeforeEach`

`configurar()` crea productos nuevos antes de cada prueba. JUnit ejecuta este
método automáticamente gracias a `@BeforeEach`. Así ninguna prueba hereda el
estado de otra y el resultado no depende del orden de ejecución.

### Pruebas de la pila

- `pilaNuevaEstaVacia` comprueba el estado inicial y una cantidad de cero.
- `pilaRespetaOrdenLifo` agrega dos objetos y verifica que salgan en orden
  inverso usando `assertSame`.
- `pilaInformaDesbordamientoYSubdesbordamiento` cubre los límites de estructura
  llena y vacía con `assertThrows`.

`assertSame` verifica que se devuelve exactamente la misma instancia. En
cambio, `assertEquals` usa el método `equals`; en `Producto`, dos instancias con
el mismo código se consideran iguales. Para enseñar el movimiento del objeto
por la estructura, la identidad es la comprobación más precisa.

### Pruebas de la cola

- `colaRespetaOrdenFifoInclusoAlDarLaVuelta` confirma FIFO y fuerza la
  reutilización circular del arreglo.
- `colaInformaDesbordamientoYSubdesbordamiento` confirma los dos errores de
  límite.

Sin el operador módulo, la primera prueba fallaría al intentar encolar el
monitor después de retirar el mouse.

### Pruebas del Repository y validaciones

- `repositorioAgregaYRetiraElProductoMasReciente` usa únicamente la API del
  repositorio y confirma que delega correctamente en la pila.
- `estructurasRechazanCapacidadInvalidaYProductosNulos` comprueba contratos de
  entrada mediante excepciones.

Una prueba debe observar comportamiento público; no debe leer el arreglo
privado ni depender de índices internos.

## 7. Cómo leer una prueba JUnit

La mayoría de las pruebas del proyecto siguen **Arrange, Act, Assert**:

```java
// Arrange: preparar el objeto y sus datos
PilaProductos pila = new PilaProductos(2);
// Act: ejecutar el comportamiento
pila.apilar(mouse);
// Assert: comprobar el resultado observable
assertSame(mouse, pila.desapilar());
```

Las aserciones usadas en el proyecto incluyen:

| Aserción | Pregunta que responde |
| --- | --- |
| `assertEquals(esperado, obtenido)` | ¿Los valores son iguales? |
| `assertSame(esperado, obtenido)` | ¿Es exactamente el mismo objeto? |
| `assertTrue(condición)` | ¿Se cumple la condición? |
| `assertNull(valor)` | ¿No se encontró ningún resultado? |
| `assertNotNull(valor)` | ¿Se creó o recuperó un resultado? |
| `assertThrows(Tipo.class, acción)` | ¿La operación rechaza correctamente el caso inválido? |
| `assertInstanceOf(Tipo.class, valor)` | ¿El objeto tiene el subtipo esperado? |

## 8. Pruebas heredadas de la semana 6

La suite existente permite explicar varios niveles de pruebas. Todas se
ejecutan con:

```bash
./mvnw test
```

### Dominio

- `ProductoTest` y `ClienteTest` prueban valores por defecto, normalización,
  setters y datos inválidos. Son pruebas unitarias porque ejercitan clases de
  dominio sin base de datos ni interfaz gráfica.
- `ModelosValorTest` verifica `Email`, `RUC` y `Monto`, incluyendo pruebas
  parametrizadas para ejecutar la misma regla con varias entradas inválidas.
- `AtributosProductoTest` comprueba la composición de atributos físicos y
  digitales y sus límites.
- `ItemProformaTest` valida cálculos con `BigDecimal`, descuentos, IVA y
  snapshots de producto.
- `ProformaTest` cubre items, totales, identidad, encapsulación de listas y
  snapshot del cliente.
- `AbstraccionTest` confirma decisiones de diseño: modelos de valor e igualdad
  por identidad lógica.

Estas pruebas muestran que una unidad no tiene que ser un solo método: puede
ser una clase pequeña con colaboradores de dominio rápidos y deterministas.

### Aplicación

`AplicacionProformasTest` usa `@BeforeEach` para construir una aplicación limpia
antes de cada caso. Comprueba CRUD, el caso de uso completo de crear una
proforma y las precondiciones cuando faltan cliente o producto. Aquí se observa
cómo probar la coordinación entre repositorios sin involucrar JavaFX.

### Persistencia SQLite

`CatalogosSQLiteTest` cubre persistencia al reabrir la base, duplicados,
búsquedas, estados y colecciones derivadas. `PersistenciaFase6Test` añade
migraciones, claves foráneas, secuencias, cascadas, snapshots y rollback de
transacciones.

Ambas clases usan `@TempDir`. JUnit entrega una carpeta temporal distinta para
la prueba, de modo que no se modifica `data/proformas.db` ni se depende de datos
creados manualmente. Reabrir un repositorio sobre el mismo archivo temporal
demuestra persistencia real, no solo estado guardado en memoria.

La prueba de rollback provoca deliberadamente un fallo con un trigger y después
comprueba que ni la cabecera ni la secuencia quedaron parcialmente guardadas.
Es un ejemplo de probar una propiedad importante, la atomicidad, y no solo el
camino feliz.

### Exportación PDF

`ExportadorProformaPDFTest` crea archivos dentro de `@TempDir`, los abre con
PDFBox y comprueba páginas y texto. También prueba paginación y rechaza
proformas incompletas. Verificar que el archivo existe no sería suficiente: un
PDF vacío o ilegible también podría existir.

### Composición JavaFX

`AplicacionJavaFXTest` verifica la ruta configurable y la composición de la
persistencia sin abrir ventanas. Separar esa lógica permite ejecutar la prueba
en terminal y evita depender de interacción humana o de un entorno gráfico.

## 9. Actividad sugerida

1. Ejecutar `EjemplosClase7.main` y predecir la salida antes de verla.
2. Comentar temporalmente el módulo de `ColaProductos` y observar qué prueba
   detecta el error.
3. Implementar `ColaClientes` y `RepositorioClientes` siguiendo FIFO.
4. Escribir primero una prueba que agregue a Ana y Luis y confirme que Ana es
   atendida primero.
5. Ejecutar toda la suite para comprobar que el cambio no rompe la semana 6.

## 10. Errores comunes

| Error | Prevención |
| --- | --- |
| Usar `cantidad` como índice antes de disminuirla al desapilar | Disminuir, leer y limpiar en ese orden |
| Tratar siempre la posición cero como frente de la cola | Mantener `frente` y actualizarlo con módulo |
| Confundir capacidad con cantidad | Guardarlas como conceptos separados |
| Devolver el arreglo interno | Exponer solo operaciones del TDA |
| Compartir estado entre pruebas | Crear datos con `@BeforeEach` o dentro de cada prueba |
| Probar solo el camino feliz | Cubrir vacío, lleno, entradas inválidas y vuelta circular |
| Poner reglas de negocio en Repository | Limitarlo a almacenar, recuperar y eliminar |

## Resumen

Pila y cola son TDA porque se definen por sus operaciones y orden, no por el
arreglo utilizado. Repository separa el acceso a los objetos de su mecanismo de
almacenamiento. JUnit convierte esos contratos en ejemplos ejecutables: prueba
LIFO, FIFO, casos límite, reglas de dominio, persistencia y efectos como PDF,
manteniendo cada caso aislado y repetible.

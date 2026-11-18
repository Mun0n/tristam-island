# Mapa del acto 1

Este documento fija la geografía y los puzles de la costa baja. No es texto de juego. La prosa en español se escribe después, tramo a tramo, y cada tramo se prueba antes de abrir el siguiente.

La referencia que manda es `tristam-en.inf` del repositorio público de Hugo Labrande, revisión `2dc9e2f2`, el bloque que el fuente titula «Act 1 - Lower level». El mapa Trizbort `design/development/1 - maps/map-lower-level.trizbort` y el borrador `design/development/3 - walkthrough.txt` se usan para contrastar. Cuando discrepan, manda el fuente publicado. Las diferencias están al final.

El acto termina en el momento en que el jugador llega arriba. Hay dos llegadas, y la primera que se consigue abre el acto 2:

- El montacargas de los acantilados del norte deja al jugador fuera del almacén, en la meseta.
- La escalera del bosque profundo deja al jugador en lo alto de la escalera, junto al poblado.

Las dos siguen existiendo después. Subir por la segunda no vuelve a puntuar el cambio de acto. Este mapa no describe el poblado.

## La orilla

Once lugares de clase `Shore`. Cada uno tiene un rumbo de mar, `vec_norm`, de 0 a 7. Ese número no es una sala: es el tramo de costa que queda a la espalda cuando el jugador entra en el agua.

| Lugar en el fuente | Rumbo | Salidas en tierra | Al agua | En `coast.json` |
| --- | --- | --- | --- | --- |
| `Immaculatebeach` | 0 | noreste al extremo de la playa; oeste a la playa del bosque. El norte es bosque cerrado y no mueve | sur | `immaculate_beach` |
| `Beachnearforest` | 0 | norte al linde; este a la playa inmaculada; oeste a los acantilados del suroeste | sur | `beach_by_forest` |
| `Edgeofthewoods` | — | norte al bosque profundo; sur a la playa del bosque | no | `edge_of_woods` |
| `Deepinthewoods` | — | sur al linde. Arriba es la escalera, y solo sube cuando el puzle está resuelto | no | `deep_woods` |
| `Southwesterncliffs` | 1 | este a la playa del bosque; noroeste al estrecho | suroeste | `southwestern_cliffs` |
| `Narrowstrait` | 2 | norte a los acantilados del noroeste; sureste a los del suroeste. El este es meterse en la poza, y no mueve | oeste | `narrow_strait` |
| `Northwesterncliffs` | 3 | sur al estrecho; noreste a los acantilados del norte | noroeste | `northwestern_cliffs` |
| `Northerncliffs` | 4 | este a la punta noreste; suroeste a los acantilados del noroeste. Arriba es el montacargas, y solo sube desde la plataforma | norte | `northern_cliffs`. El bote se examina. La plataforma todavía no sube |
| `Northeasterntip` | 5 | oeste a los acantilados del norte; sureste a la cala. El suroeste de tierra es infranqueable | noreste | `northeastern_tip` |
| `Cove` | 6 | sur al extremo de la playa; noroeste a la punta noreste | este | `cove` |
| `Endofthebeach` | 7 | norte a la cala; suroeste a la playa inmaculada | sureste | `end_of_beach` |

La costa es un anillo. Desde la playa inmaculada se puede dar la vuelta entera: oeste por los acantilados, norte hasta el muelle, este hasta la cala y sur de vuelta al peñasco. El bosque es un ramal de tres pasos hacia el norte desde la playa del bosque, y allí se acaba.

En `coast.json` ese anillo se camina, y desde cada orilla se entra en el agua. El noreste de los acantilados del noroeste llega al muelle, y la cala vuelve a la punta noreste. Mirar el bote no puntúa. La plataforma se mira y no sube.

## El océano

`OnlyRoomWaistDeep` es un solo lugar. El mapa de diseño lo dibuja como ocho salas. El fuente no. El plan de adaptación ya lo dice: el océano es un lugar con rumbo y profundidad.

Al entrar, el rumbo copia el `vec_norm` de la orilla. Caminar en el agua sigue la costa y va cambiando la orilla de referencia. Las dos playas del sur comparten el rumbo 0: ir de una a la otra por el agua no gira el rumbo. El rumbo que apunta a tierra devuelve a esa orilla. El rumbo que se aleja demasiado no deja avanzar.

Dos rumbos importan.

El rumbo 1 es el agua frente a los acantilados del suroeste. El nombre visible pasa a ser agua hasta la rodilla, porque hay un banco de arena. Cada paso al suroeste suma uno a `sandbankDepth`. A 1 ya se distingue un mástil entre los árboles. Examinarlo la primera vez descubre que es una torre de vigilancia y suma 1. A más de 8 se intuye algo bajo en el horizonte, hacia el suroeste. A 10 el fondo cae y el paso no cuenta. Volver hacia tierra resta profundidad. Salir del rumbo 1 la pone a cero. Identificar esa tierra lejana con los prismáticos suma 5, pero los prismáticos están en la tienda del acto 2. En este acto el horizonte se ve y no se nombra.

El rumbo 2 es el agua al oeste del estrecho. El rumbo 6 es el agua al este de la cala. En los dos hay peces, porque el fuente los muestra cuando `vecteur_normal % 4` vale 2. En el resto del agua no hay peces. El muelle se ve desde el rumbo 4 y no se puede tocar.

Los guijarros de la orilla rocosa se pueden tirar al mar. El número de rebotes es aleatorio en el fuente. Aquí se fija una frase, como ya dice el plan. No cambian el estado.

## Objetos que salen en este acto

Las cerillas empiezan en el inventario. El límite es tres objetos en la mano. Las monedas no cuentan, pero en este acto no hay monedas. La mochila aparece en el almacén del acto 2.

| Objeto | Dónde está | Qué hace en este acto |
| --- | --- | --- |
| Cerillas | Inventario inicial | Encienden la hoguera. Se consumen al cocinar el pez |
| Flor | Árboles del linde y del bosque profundo | Una cada vez. Se pone en el anzuelo como reclamo. Si se suelta, el viento se la lleva |
| Gusano | Bajo la roca musgosa del linde | Uno cada vez, cuando el anterior ya no existe. Se pone en el anzuelo como cebo. Si se suelta, se escapa |
| Ramas | Linde y bosque profundo | Se convierten en leña. Solo hace falta una carga antes de subir |
| Leña | En la mano al cortar ramas | Se seca al sol si no está en el bosque. En la costa norte se seca el doble de rápido. Húmeda no arde. Seca, al soltarla, queda montada la hoguera |
| Caña, sedal y anzuelo | Un solo objeto, dentro del compartimento del bote | El sedal y el anzuelo ya van montados. No se cogen aparte |
| Red | El mismo compartimento | No sirve para pescar solo. Atada a dos árboles del linde o del bosque profundo se vuelve hamaca y sale del inventario |
| Cuchillo de destripar | El mismo compartimento | Hace falta para vaciar el pez. Si se lleva encima al coger leña, se queda en el suelo de ese lugar |
| Alga | Bajo el casco del bote | Se come una vez, ya seca al sol del muelle. Suma 1. No cuenta como la comida que permite subir |
| Pez | Se pesca | El primero atrae al albatros. El segundo se cocina |
| Hamaca | El lugar donde se ató la red | Dormir ahí, una vez, suma 5 |
| Flauta | La deja el albatros después del primer pez | Se guarda. Sus usos están en el acto 2, salvo uno: llamarla en un sitio con peces, sin pez en la mano, da un pez y consume la flauta |
| Plataforma y cuerda | Acantilados del norte, y luego también fuera del almacén | Suben y bajan. Abajo, sin estar encima, la cuerda solo levanta la plataforma unos palmos |

El bote está amarrado al muelle de los acantilados del norte. El casco está rajado y no navega. El compartimento de popa empieza cerrado.

El texto del gusano dice que esa mano no puede cargar nada más. El código no añade esa regla: solo aplica el límite de tres. Pescar también dice que hacen falta las dos manos, y el código lo traduce así: con tres objetos encima no se puede lanzar la caña. Flor y gusano dejan de ocupar mano en cuanto están en el anzuelo.

## Lo que hay que conseguir antes de subir

Las dos salidas piden lo mismo: haber comido el pez (`F_ATE_SOMETHING`) y haber dormido en la hamaca (`F_SLEPT`). El alga no sustituye al pez. Dormir en la arena, en el suelo del bosque, en el agua o en la plataforma no cuenta.

El pez se cocina así.

1. Abrir el compartimento y coger la caña, la red y el cuchillo. La capacidad obliga a hacer varios viajes.
2. Coger un gusano y una flor. Poner el gusano en el anzuelo suma 1 la primera vez. Poner la flor suma 1 la primera vez.
3. Entrar en el agua del estrecho o en el agua de la cala, con la caña y sin el inventario lleno. El primer pez suma 5 y llama al albatros.
4. Al salir a tierra el albatros aterriza y sigue al jugador mientras ese pez exista. Dárselo, soltar la caña delante de él o intentar esconder el pez se lo entrega. Se come el pez, deja la flauta y se va. Intentar cocinar con el albatros delante también le da el pez, y no hay comida.
5. Volver a coger gusano y flor, que el primer lance consumió, y pescar otro. El segundo pez no suma y no llama al pájaro.
6. Cortar ramas. La primera carga, antes de subir, suma 5. Dejar la leña al sol fuera del bosque. En la costa norte pasa de humedad 5 a 3 y luego a 1. En el resto de la orilla baja de uno en uno. En el bosque no baja. Con humedad menor que 2, soltarla monta la hoguera.
7. Con la hoguera montada, el pez, las cerillas y el cuchillo en la mano, y sin el albatros, cocinar. Se consumen la leña, las cerillas y el pez. Suma 5 y queda hecha la comida.

Dormir es independiente. Atar la red entre los árboles del linde o del bosque profundo, y dormir en ese lugar. Suma 5 una sola vez. Se puede dormir antes o después de comer.

Con las dos cosas hechas, cualquiera de estas acciones sube y suma 5 una sola vez:

- En los acantilados del norte, subirse a la plataforma y tirar de la cuerda. Sin comida o sin sueño, el jugador se detiene a los pocos palmos para no caer. Con las dos, llega fuera del almacén.
- En el bosque profundo, rascar el barro con el cuchillo. Sin comida o sin sueño, se limpia un peldaño, se entiende que aquellas piedras son una escalera y el trabajo se deja por hambre o por cansancio. Con las dos y el cuchillo en la mano, el corte dura lo que el fuente cuenta como dos días y el jugador llega arriba de la escalera. Sin cuchillo, las manos no sirven.

Examinar las piedras también descubre la escalera y suma 1, aunque no se intente limpiar. `coast.json` ya da ese punto al examinarlas. El intento de subir, hoy, siempre responde lo mismo. En el fuente la respuesta cambia: primero son piedras peligrosas, luego una escalera enterrada, y solo al final un camino.

## Puntos de este acto

| Hecho | Puntos | ¿Hace falta para subir? |
| --- | --- | --- |
| Poner el primer gusano en el anzuelo | 1 | Sí |
| Poner la primera flor en el anzuelo | 1 | Sí |
| Coger leña por primera vez | 5 | Sí |
| Pescar el primer pez | 5 | Sí, porque llama al albatros; el pez que se come es el segundo |
| Comer el pez cocinado | 5 | Sí |
| Dormir en la hamaca | 5 | Sí |
| Reconocer la escalera | 1 | Solo si se sube por ella, o si se examina |
| Ver la torre desde el banco de arena | 1 | No |
| Comer el alga | 1 | No |
| Llegar arriba por primera vez | 5 | Es el cierre |

El mínimo para salir por el montacargas, sin torre, sin alga y sin mirar la escalera, es 27. Mirar el bote no suma. Coger la flor tampoco: el punto es enanzuelarla.

## Dónde el borrador no es el juego

El walkthrough de diseño se escribió antes de cerrar el fuente. Estas diferencias se respetan a favor del fuente.

- Beber quedó anotado como «todavía no decidido, quizá una flor cóncava». En el juego publicado se bebe en la fuente del poblado, ya en el acto 2. En la costa, el agua de mar no se bebe y el alga no quita la sed que abre el camino.
- El borrador coge sedal, anzuelo y cuchillo bowie por separado. El juego publica una caña ya montada y un cuchillo de destripar.
- El borrador dibuja el océano como varias salas y dice diez pasos al suroeste. El juego usa un lugar, un rumbo y una profundidad que se corta al pasar de 9.
- El borrador da el primer pez al albatros y cocina el segundo. Eso sí está en el fuente. Cocinar el primero, con el pájaro delante, lo pierde.

## Lo que la costa actual ya hace

Están las once orillas, el océano como un solo lugar, el prólogo, las cerillas, la flor y el gusano. El rumbo y la profundidad se guardan en la partida. El banco de arena está frente a los acantilados del suroeste: a partir de un paso se ve el mástil, y examinarlo suma 1. Pasado 9 el fondo cae. Los peces están en los rumbos 2 y 6.

El compartimento del bote se abre. La caña, la red y el cuchillo se cogen, con el límite de tres objetos en la mano. El alga suma 1 y no cuenta como comida. El primer gusano y la primera flor en el anzuelo suman 1 cada uno. El primer pez suma 5 y llama al albatros. Dárselo en tierra quita el pez y deja la flauta. El segundo pez no suma.

La plataforma se mira y no se usa. El intento de subir la ladera sigue siendo un rechazo.

La leña se corta en el linde o en el bosque. Si llevas el cuchillo, se queda en el suelo para poder cargar los palos. En la costa norte se seca de dos en dos por turno; en el resto de la orilla, de uno en uno; en el bosque no se seca. Con humedad menor que 2 se monta la hoguera. Cocinar gasta la leña, las cerillas y el pez, y pide el cuchillo y que el albatros ya se haya ido. La red atada en el linde o en el bosque es la hamaca. Dormir ahí suma 5. Con la comida y el sueño, la plataforma o la escalera suben a la meseta y suman 5 una sola vez. Arriba ya se puede recorrer el poblado por fuera: el huerto, el pub, la plaza, la fuente, las casas, la tienda, el taller, los generadores y la verja. El almacén deja coger la linterna. El agujero de los arbustos y el árbol del borde entran al campo, y desde ahí se ve el porche de la casa blanca. La casa del tabernero se abre: la caja fuerte da la llave del pub, y el mapa de la pared dice que esta isla es Kingsley y que Tristam está al suroeste. Una piedra del mirador rompe la tienda y el taller. Los prismáticos, desde la torre ya reparada, muestran Tristam quemada. El destornillador saca la puerta tosca, y el libro da el código 5-2-1-8. Prismáticos, mechero, chicle, moneda, sierra, peldaños y destornillador se pueden dejar en el suelo y recoger; el mostrador no da una segunda copia. La cocina del pub da una olla y un bloque de aceite, cortado con el cuchillo. El cuchillo se queda en la cocina. El bloque se mete en la olla, se derrite en la hoguera del mirador con el mechero, y ese líquido llena el depósito de los generadores. La manivela, con el depósito lleno, suma 5 y enciende la luz. El código 5-2-1-8 abre la mansión y suma 5. Dentro se recorre el recibidor, la biblioteca, el comedor, la entreplanta y dos dormitorios. La bola, la bomba, la estatua y el tornillo del tubo abren la puerta del laboratorio y suman, en ese orden, 1 por el primer aviso, 5 por la primera puerta de dormitorio, 2 por conectar la bomba, 2 por girar la estatua y 5 por la puerta. La antesala tiene el ascensor. Bajar suma 5 y abre el laboratorio: vestíbulo, pasillos, dos salas, la pizarra, la sala de reuniones, el ventanal de la gruta y las taquillas. El cartel de la bomba suma 5. La carta de Grant, del 15 de febrero de 1962, no suma. El candado de la taquilla cede si se escucha, y suma 5. Dentro hay un cable. La silla de la sala de reuniones, con el libro enorme o con la caja del vestíbulo, deja apartar una loseta y entrar al despacho por el techo: eso suma 5. El libro grueso se queda corto. El llavero del cajón suma 5 y abre el despacho y la sala de comunicaciones. La puerta de la escalera no. En la sala, cambiar el cable derretido suma 5. El dial empieza en 12.4020 Mc: a la izquierda resta 13 y a la derecha suma 20, y la banda se corta en 12.3895 y 12.4126. Caer en 12.4035 suma 5. Con los auriculares, el cable nuevo y esa marca, la llamada a los guardacostas suma 5 y cierra el rescate. La mochila no está escrita.

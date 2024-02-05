# Защита информации

Лаборторные работы из университета. Для шифровки и генерации ключей используется openssl.

## Лабораторная №1

Напишите программу для внедрения в файл 28.bmp и извлечения из него хешкода файла leasing.txt, полученного с помощью алгоритма SHA-1. Метод реализации – LSB Replacement. Номера байтов-контейнеров должны содержаться в предварительно сгенерированном ключе. С помощьюсторонних приложений оцените объемы получившегося и исходного изображений после сжатия.

## Лабораторная №2

Напишите программу, шифрующую изображение tux.png (формат не принципиален) с помощью шифра AES. Режимы шифрования: ECB, CBC, CFB и OFB (нужно получить четыре варианта зашифрованного изображения). В учебных целях заголовочную часть файла зашифровывать не нужно. Сравните скорости выполнения алгоритмов и результаты шифрования.

## Лабораторная №3

Реализуйте простое клиент-серверное приложение, позволяющее аккумулировать короткие анонимные сообщения (систему электронного голосования) согласно следующей схеме:

![alt text](https://i.imgur.com/UuCoCoO.png)

Здесь: A – пользователь (избиратель), B – регистратор, C – счетчик, x – сообщение (голос), r – известное только участнику A случайное число, (e, n) – открытый ключ банка. Пренебрегите реализацией правильных механизмов распределения, хранения и сертификации ключей.

## Лабораторная №4

Реализуйте генератор псевдослучайной последовательности битов на основе регистра сдвига с линейной обратной связью (РСЛОС) в конфигурации Галуа. Начальное значение сдвигового регистра и его образующий многочлен должны задаваться пользователем. Результат представьте в виде точечной диаграммы, где по горизонтали отложены порядковые номера генерируемых битов, а по вертикали – их значения. С помощью критерия χ^2 оцените качество любой генерируемой последовательности максимальной длины. Путем однократного гаммирования, не затрагивая заголовочную часть, зашифруйте изображение tux.png (формат не принципиален), порциями по 8 бит. Объясните результат.

## Лабораторная №5

Напишите программу, генерирующую и визуализирующую все решения уравнения вида y 2 ≡ x 3 + ax + b (mod p), где a, b ∈ Zp, где p – простое число (т.е. точки произвольной эллиптической кривой над конечным полем). Реализуйте операции (с наглядным представлением результата): 1) сложения двух точек кривой; 2) удвоения точки кривой.

## Лабораторная №6

Напишите простое клиент-серверное приложение, в котором сервер выступает в качестве удостоверяющего центра, а клиенты могут обмениваться подписанными документами (с возможностью проверки подписей). Шифр: RSA.
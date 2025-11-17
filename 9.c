#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "student_manager.h"

// Функция для создания менеджера студентов
StudentManager* create_manager(int capacity) {
    StudentManager* manager = (StudentManager*)malloc(sizeof(StudentManager));
    manager->students = (Student*)malloc(capacity * sizeof(Student));
    manager->count = 0;
    manager->capacity = capacity;
    return manager;
}

// Функция для добавления студента
void add_student(StudentManager* manager, const char* name, int id) {
    if (manager->count >= manager->capacity) {
        printf("Ошибка: достигнута максимальная вместимость\n");
        return;
    }
    
    // ИСПРАВЛЕНИЕ 1: Проверка длины имени для предотвращения переполнения буфера
    if (strlen(name) >= 50) {
        printf("Ошибка: имя слишком длинное (максимум 49 символов)\n");
        return;
    }
    
    Student* student = &manager->students[manager->count];
    strcpy(student->name, name);
    student->id = id;
    student->grades = NULL;
    student->grades_count = 0;
    student->average = 0.0f;
    
    manager->count++;
    printf("Добавлен студент: %s (ID: %d)\n", name, id);
}

// Функция для добавления оценки студенту
void add_grade(StudentManager* manager, int student_id, float grade) {
    // ИСПРАВЛЕНИЕ 2: Исправлены границы цикла - было i <= manager->count
    // Теперь цикл идет только по существующим студентам (i < manager->count)
    for (int i = 0; i < manager->count; i++) {
        if (manager->students[i].id == student_id) {
            Student* student = &manager->students[i];
            
            // ИСПРАВЛЕНИЕ 3 и 4: Правильное выделение памяти с проверкой
            // Было: student->grades_count * sizeof(float) - выделяло 0 байт для первой оценки
            // Стало: (student->grades_count + 1) * sizeof(float) - выделяет достаточно памяти
            float* new_grades = (float*)realloc(student->grades, 
                                            (student->grades_count + 1) * sizeof(float));
            
            // Проверка успешности выделения памяти
            if (new_grades == NULL) {
                printf("Ошибка выделения памяти для оценок студента %s\n", student->name);
                return;
            }
            student->grades = new_grades;
            
            // Добавление новой оценки
            student->grades[student->grades_count] = grade;
            student->grades_count++;
            
            // ИСПРАВЛЕНИЕ 5: Правильный расчет среднего балла
            // Было: student->average = (student->average + grade) / student->grades_count;
            // Эта формула неверна 
            // Стало: пересчет полной суммы всех оценок
            float sum = 0.0f;
            for (int j = 0; j < student->grades_count; j++) {
                sum += student->grades[j];
            }
            student->average = sum / student->grades_count;
            
            printf("Добавлена оценка %.2f студенту %s\n", grade, student->name);
            return;
        }
    }
    printf("Студент с ID %d не найден\n", student_id);
}

// Функция для поиска студента по ID
Student* find_student(StudentManager* manager, int student_id) {
    // ИСПРАВЛЕНИЕ 6: Исправлены границы цикла - было i <= manager->capacity
    // Теперь цикл идет только по существующим студентам (i < manager->count)
    // Использование capacity вместо count приводило к чтению неинициализированной памяти
    for (int i = 0; i < manager->count; i++) {
        if (manager->students[i].id == student_id) {
            return &manager->students[i];
        }
    }
    return NULL;
}
// Функция для вывода информации о студенте
void print_student(StudentManager* manager, int student_id) {
    Student* student = find_student(manager, student_id);
    if (student) {
        printf("Студент: %s (ID: %d)\n", student->name, student->id);
        printf("Оценки: ");
        for (int i = 0; i < student->grades_count; i++) {
            printf("%.2f ", student->grades[i]);
        }
        printf("\nСредний балл: %.2f\n", student->average);
    } else {
        printf("Студент с ID %d не найден\n", student_id);
    }
}

// Функция для освобождения памяти
void free_manager(StudentManager* manager) {
    for (int i = 0; i < manager->count; i++) {
        free(manager->students[i].grades);
    }
    free(manager->students);
    free(manager);
}

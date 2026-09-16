package com.kronos.skilletapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kronos.skilletapp.database.dao.EquipmentDao
import com.kronos.skilletapp.database.dao.IngredientDao
import com.kronos.skilletapp.database.dao.InstructionDao
import com.kronos.skilletapp.database.dao.RecipeDao
import com.kronos.skilletapp.database.entity.EquipmentEntity
import com.kronos.skilletapp.database.entity.IngredientEntity
import com.kronos.skilletapp.database.entity.InstructionEquipmentEntity
import com.kronos.skilletapp.database.entity.InstructionEntity
import com.kronos.skilletapp.database.entity.InstructionIngredientEntity
import com.kronos.skilletapp.database.entity.RecipeEntity

@Database(
  entities =
    [
      RecipeEntity::class,
      IngredientEntity::class,
      InstructionEntity::class,
      EquipmentEntity::class,
      InstructionIngredientEntity::class,
      InstructionEquipmentEntity::class,
    ],
  version = 2,
)
@TypeConverters(MeasurementConverters::class)
abstract class RecipeDatabase : RoomDatabase() {
  abstract fun recipeDao(): RecipeDao

  abstract fun ingredientDao(): IngredientDao

  abstract fun equipmentDao(): EquipmentDao

  abstract fun instructionDao(): InstructionDao
}

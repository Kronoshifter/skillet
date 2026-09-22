package com.kronos.skilletapp.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("DROP TABLE IF EXISTS `recipe`")
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `recipe` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `cover` TEXT, `notes` TEXT NOT NULL, `servings` INTEGER NOT NULL, `prep_time` INTEGER NOT NULL, `cook_time` INTEGER NOT NULL, `source_name` TEXT NOT NULL, `source_url` TEXT NOT NULL, PRIMARY KEY(`id`))"
    )
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `ingredient` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `name` TEXT NOT NULL, `measurement` TEXT NOT NULL, `raw` TEXT NOT NULL, `comment` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `instruction` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `text` TEXT NOT NULL, `image` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `equipment` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `instruction_ingredient` (`instruction_id` TEXT NOT NULL, `ingredient_id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`instruction_id`, `ingredient_id`), FOREIGN KEY(`instruction_id`) REFERENCES `instruction`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredient_id`) REFERENCES `ingredient`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
    db.execSQL(
      "CREATE TABLE IF NOT EXISTS `instruction_equipment` (`instruction_id` TEXT NOT NULL, `equipment_id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`instruction_id`, `equipment_id`), FOREIGN KEY(`instruction_id`) REFERENCES `instruction`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`equipment_id`) REFERENCES `equipment`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    )
  }
}

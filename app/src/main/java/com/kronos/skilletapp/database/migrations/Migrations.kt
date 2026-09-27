package com.kronos.skilletapp.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL

val MIGRATION_1_2 =
  object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
      connection.executeSQL("DROP TABLE IF EXISTS `recipe`")
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `recipe` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `cover` TEXT, `notes` TEXT NOT NULL, `servings` INTEGER NOT NULL, `prep_time` INTEGER NOT NULL, `cook_time` INTEGER NOT NULL, `source_name` TEXT NOT NULL, `source_url` TEXT NOT NULL, PRIMARY KEY(`id`))"
      )
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `ingredient` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `name` TEXT NOT NULL, `measurement` TEXT NOT NULL, `raw` TEXT NOT NULL, `comment` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
      )
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `instruction` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `text` TEXT NOT NULL, `image` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
      )
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `equipment` (`id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`recipe_id`) REFERENCES `recipe`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
      )
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `instruction_ingredient` (`instruction_id` TEXT NOT NULL, `ingredient_id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`instruction_id`, `ingredient_id`), FOREIGN KEY(`instruction_id`) REFERENCES `instruction`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`ingredient_id`) REFERENCES `ingredient`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
      )
      connection.executeSQL(
        "CREATE TABLE IF NOT EXISTS `instruction_equipment` (`instruction_id` TEXT NOT NULL, `equipment_id` TEXT NOT NULL, `recipe_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`instruction_id`, `equipment_id`), FOREIGN KEY(`instruction_id`) REFERENCES `instruction`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`equipment_id`) REFERENCES `equipment`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
      )
    }
  }

val MIGRATION_2_3 =
  object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
      connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_recipe_name` ON `recipe` (`name`)")
      connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_ingredient_recipe_id` ON `ingredient` (`recipe_id`)")
      connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_instruction_recipe_id` ON `instruction` (`recipe_id`)")
      connection.executeSQL("CREATE INDEX IF NOT EXISTS `index_equipment_recipe_id` ON `equipment` (`recipe_id`)")
      connection.executeSQL(
        "CREATE INDEX IF NOT EXISTS `index_instruction_ingredient_instruction_id` ON `instruction_ingredient` (`instruction_id`)"
      )
      connection.executeSQL(
        "CREATE INDEX IF NOT EXISTS `index_instruction_ingredient_ingredient_id` ON `instruction_ingredient` (`ingredient_id`)"
      )
      connection.executeSQL(
        "CREATE INDEX IF NOT EXISTS `index_instruction_equipment_instruction_id` ON `instruction_equipment` (`instruction_id`)"
      )
      connection.executeSQL(
        "CREATE INDEX IF NOT EXISTS `index_instruction_equipment_equipment_id` ON `instruction_equipment` (`equipment_id`)"
      )
    }
  }

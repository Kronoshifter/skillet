import ingredient_slicer
import json

def parse(text):
    # parsed = parse_ingredient(text)

    # ingredient = {
    #     "name": parsed.name.text,
    #     "raw": parsed.sentence,
    #     "comment": parsed.comment,
    #     "measurement": {
    #         "quantity": parsed.amount[0].quantity,
    #         "unit": str(parsed.amount[0].unit),
    #     }
    # }

    # return json.dumps(ingredient)
    slicer = ingredient_slicer.IngredientSlicer(text)
    return json.dumps(slicer.to_json())
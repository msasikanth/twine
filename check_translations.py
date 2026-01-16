import os
import xml.etree.ElementTree as ET

def get_keys(xml_path):
    if not os.path.exists(xml_path):
        return set()
    tree = ET.parse(xml_path)
    root = tree.getroot()
    keys = set()
    for child in root:
        name = child.get('name')
        if name:
            keys.add(name)
    return keys

def check_missing_keys(base_path, lang_paths):
    base_keys = get_keys(base_path)
    for lang, path in lang_paths.items():
        lang_keys = get_keys(path)
        missing = base_keys - lang_keys
        if missing:
            print(f"Language: {lang} is missing {len(missing)} keys: {missing}")
        else:
            print(f"Language: {lang} is up to date.")

shared_base = "shared/src/commonMain/composeResources/values/strings.xml"
shared_langs = {
    "de": "shared/src/commonMain/composeResources/values-de/strings.xml",
    "hi": "shared/src/commonMain/composeResources/values-hi/strings.xml",
    "ru": "shared/src/commonMain/composeResources/values-ru/strings.xml",
    "fr": "shared/src/commonMain/composeResources/values-fr/strings.xml",
    "tr": "shared/src/commonMain/composeResources/values-tr/strings.xml",
    "zh": "shared/src/commonMain/composeResources/values-zh/strings.xml",
}

print("Checking shared strings:")
check_missing_keys(shared_base, shared_langs)

android_base = "androidApp/src/main/res/values/strings.xml"
android_langs = {
    "de": "androidApp/src/main/res/values-de/strings.xml",
    "hi": "androidApp/src/main/res/values-hi/strings.xml",
    "ru": "androidApp/src/main/res/values-ru/strings.xml",
    "fr": "androidApp/src/main/res/values-fr/strings.xml",
    "tr": "androidApp/src/main/res/values-tr/strings.xml",
    "zh": "androidApp/src/main/res/values-zh/strings.xml",
}

print("\nChecking android strings:")
check_missing_keys(android_base, android_langs)

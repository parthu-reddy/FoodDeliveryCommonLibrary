import os
import re

dir_path = "/Users/parthureddy/Documents/Food Delivery.nosync/CommonLibrary/src/main/java/com/fooddelivery/common"

for root, _, files in os.walk(dir_path):
    for file in files:
        if file.endswith(".java"):
            filepath = os.path.join(root, file)
            with open(filepath, "r") as f:
                content = f.read()
            
            if re.search(r'@(Component|Service|Configuration|Controller|RestController)', content) and not "RequiredArgsConstructor" in content:
                # add @lombok.RequiredArgsConstructor right above public class
                if "public class" in content:
                    content = content.replace("public class", "@lombok.RequiredArgsConstructor\npublic class")
                    with open(filepath, "w") as f:
                        f.write(content)
                    print("Fixed:", filepath)

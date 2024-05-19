#!/bin/bash
# Create or clear the output file
output_file="all.txt"
> "$output_file"

# Append the `tree` command output with a max depth of 3
echo "Directory tree with max depth of 3:" >> "$output_file"
echo '```' >> "$output_file"
tree -L 5 -I build >> "$output_file"
echo '```' >> "$output_file"
echo -e "\n\n" >> "$output_file"

# Find and process relevant files, excluding those in /build/ directories
find . -type f \( -name "*.kt" -o -name "*.md" -o -name "*.yml" -o -name "*.kts" -o -name "*.sh" -o -name "template.env" -o -name "Dockerfile" \) ! -path "*/build/*" | while IFS= read -r file; do
  echo "#### File: \`$file"\` >> "$output_file"
  echo '```' >> "$output_file"
  echo -e "\n" >> "$output_file"
  cat "$file" >> "$output_file"
  echo -e "\n" >> "$output_file"
  echo '```' >> "$output_file"
  echo -e "\n\n" >> "$output_file"  # Adding newlines to separate file contents
done

# copy all.txt contents to clipboard with xclip
cat "$output_file" | xclip -selection clipboard
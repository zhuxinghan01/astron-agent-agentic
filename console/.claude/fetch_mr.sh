#!/bin/bash
# 获取 GitLab MR 的 diff 信息
# 用法: ./.claude/fetch_mr.sh <MR编号> [--diff-only] [--summary]
#
# 示例:
#   ./.claude/fetch_mr.sh 816             # 输出元数据 + 文件变更摘要 + 完整diff
#   ./.claude/fetch_mr.sh 816 --summary   # 仅输出元数据 + 文件变更摘要
#   ./.claude/fetch_mr.sh 816 --diff-only # 仅输出完整diff

source ~/.zshrc 2>/dev/null

GITLAB_URL="https://git.iflytek.com"
PROJECT_ID="49707"
MR_NUMBER="$1"
MODE="${2:-all}"

if [ -z "$MR_NUMBER" ]; then
  echo "用法: $0 <MR编号> [--summary|--diff-only]"
  exit 1
fi

if [ -z "$GITLAB_TOKEN" ]; then
  echo "错误: GITLAB_TOKEN 未设置"
  exit 1
fi

OUTPUT_FILE="/tmp/mr${MR_NUMBER}_changes.json"
CHANGES_URL="${GITLAB_URL}/api/v4/projects/${PROJECT_ID}/merge_requests/${MR_NUMBER}/changes?access_raw_diffs=true"

curl -sS --header "PRIVATE-TOKEN: ${GITLAB_TOKEN}" \
  "${CHANGES_URL}" \
  -o "$OUTPUT_FILE"

if [ $? -ne 0 ] || [ ! -s "$OUTPUT_FILE" ]; then
  echo "错误: 获取 MR #${MR_NUMBER} 失败"
  exit 1
fi

MR_NUMBER="$MR_NUMBER" MODE="$MODE" OUTPUT_FILE="$OUTPUT_FILE" python3 << 'PYEOF'
import json, sys, os

mr_number = os.environ.get("MR_NUMBER")
mode = os.environ.get("MODE", "all")
output_file = os.environ.get("OUTPUT_FILE")

with open(output_file) as f:
    data = json.load(f)

if "message" in data and "changes" not in data:
    print(f"错误: {data['message']}")
    sys.exit(1)

changes = data.get("changes", [])

# --- 元数据 ---
if mode != "--diff-only":
    print(f"=== MR #{mr_number} ===")
    print(f"Title:  {data.get('title', '')}")
    print(f"Author: {data.get('author', {}).get('name', '')}")
    print(f"State:  {data.get('state', '')}")
    print(f"Source: {data.get('source_branch', '')} → {data.get('target_branch', '')}")
    desc = (data.get("description") or "")[:200]
    if desc:
        print(f"Desc:   {desc}")
    print()

    # --- 文件变更摘要 ---
    total_add, total_del = 0, 0
    print(f"Changed files: {len(changes)}")
    print(f"{'File':<100} {'Added':>6} {'Deleted':>8}")
    print("-" * 118)
    for c in changes:
        diff = c.get("diff", "")
        lines = diff.split("\n")
        added = sum(1 for l in lines if l.startswith("+") and not l.startswith("+++"))
        deleted = sum(1 for l in lines if l.startswith("-") and not l.startswith("---"))
        total_add += added
        total_del += deleted
        path = c.get("new_path", "unknown")
        flag = " (new)" if c.get("new_file") else (" (del)" if c.get("deleted_file") else (" (rename)" if c.get("renamed_file") else ""))
        print(f"{path + flag:<100} +{added:<5} -{deleted:<7}")
    print("-" * 118)
    print(f"{'Total':<100} +{total_add:<5} -{total_del:<7}")
    missing_diff_paths = [
        c.get("new_path", "unknown")
        for c in changes
        if not (c.get("diff") or "").strip()
    ]
    if missing_diff_paths:
        print("Warning: API returned empty diff for:")
        for path in missing_diff_paths:
            print(f"  - {path}")
    print()

# --- 完整 diff ---
if mode != "--summary":
    for c in changes:
        path = c.get("new_path", "unknown")
        diff = c.get("diff", "")
        if diff.strip():
            print(f"### {path}")
            print(diff)
            print()
PYEOF

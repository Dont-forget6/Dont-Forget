# Dont-Forget

✔️ TODO 일정 관리 서비스

---

## ✅ Commit Convention
**Message Structure**
```
[type] Subject

body

footer
```
**The Type**
|    Type    | Description                 |
|:----------:|-----------------------------|
|   `Feat`   | 신규 기능 구현 작업                 |
|   `Fix`   | 기능 수정 작업                 |
|   `Design`   | 사용자 UI 디자인 변경 작업                 |
|   `Style`   | 코드 스타일 관련 작업 (코드 의미 변경 X) |
|  `Rename`  | 파일/폴더 명 또는 변수/클래스/메서드 명 변경             |
|  `Remove`  | 파일/폴더 삭제             |
|  `Chore`   | configs 변화 등 그 외 작업 (코드 변경 X) |
| `Refactor` | 리팩토링 작업                     |
|   `Docs`   | 문서 관련 작업                 |

**예시**
```
[Feat] 소셜 로그인 기능 구현

카카오 소셜 로그인 구현

Resolves: #21 or Fixes: #21
```

## 🧱 Branch Strategy
- Git flow
  - main
    - 배포 target
  - dev
  - feature/**
    - 예시: feature/calendar
  - hotfix/**
    - 예시: hotfix/login

## 👨‍💻 Code review
- PR 시 2명 이상의 approve를 받은 경우에 dev 에 merge

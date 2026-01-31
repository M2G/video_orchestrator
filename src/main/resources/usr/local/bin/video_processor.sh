#!/bin/bash

#############################################
# Video Processor - Script de surveillance
# Traite automatiquement les MP4 d'un dossier
#############################################

# Configuration
WATCH_DIR="$HOME/Works/video_orchestrator/src/main/resources/tmp/videos"
OUTPUT_DIR="$HOME/Works/video_orchestrator/src/main/resources/var/www/html/streams"
PROCESSING_DIR="$HOME/Works/video_orchestrator/src/main/resources/tmp/videos/processing"
DONE_DIR="$HOME/Works/video_orchestrator/src/main/resources/tmp/videos/done"
ERROR_DIR="$HOME/Works/video_orchestrator/src/main/resources/tmp/videos/error"
LOG_FILE="$HOME/Works/video_orchestrator/src/main/resources/var/log/video_processor.log"
LOCK_FILE="$HOME/Works/video_orchestrator/src/main/resources/var/run/video_processor.lock"


#WATCH_DIR="./tmp/videos"
#OUTPUT_DIR="./var/www/html/streams"
#PROCESSING_DIR="./tmp/videos/processing"
#DONE_DIR="./tmp/videos/done"
#ERROR_DIR="./tmp/videos/error"
#LOG_FILE="./var/log/video_processor.log"
#LOCK_FILE="./var/run/video_processor.lock"

# Paramètres de segmentation
SEGMENT_DURATION=10
MAX_SEGMENTS=0
EXTENSION=".ts"

# Chemin vers le binaire
#SEGMENTER="./usr/local/bin/video_segmenter"
SEGMENTER="$HOME/Works/video_orchestrator/src/main/resources/usr/local/bin/video_segmenter"

#############################################
# Fonctions utilitaires
#############################################

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" | tee -a "$LOG_FILE" >&2
}

# Crée les dossiers nécessaires
init_directories() {
    mkdir -p "$WATCH_DIR" "$OUTPUT_DIR" "$PROCESSING_DIR" "$DONE_DIR" "$ERROR_DIR"
    mkdir -p "$(dirname "$LOG_FILE")"
    mkdir -p "$(dirname "$LOCK_FILE")"
    touch "$LOG_FILE"
}

# Vérifie si un fichier est en cours d'écriture (compatible Linux et macOS)
is_file_stable() {
    local file="$1"

    # Vérifie que le fichier existe
    if [ ! -f "$file" ]; then
        log "Fichier introuvable: $file"
        return 1
    fi

    # Détection du système d'exploitation
    local size1 size2
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        size1=$(stat -f%z "$file" 2>/dev/null || echo 0)
        sleep 2
        size2=$(stat -f%z "$file" 2>/dev/null || echo 0)
    else
        # Linux
        size1=$(stat -c%s "$file" 2>/dev/null || echo 0)
        sleep 2
        size2=$(stat -c%s "$file" 2>/dev/null || echo 0)
    fi

    log "Vérification stabilité: taille1=$size1, taille2=$size2"

    if [ "$size1" -eq "$size2" ] && [ "$size1" -gt 0 ]; then
        return 0  # Stable
    else
        return 1  # En cours d'écriture
    fi
}

# Vérifie le lock pour éviter plusieurs instances
acquire_lock() {
    if [ -f "$LOCK_FILE" ]; then
        local pid=$(cat "$LOCK_FILE")
        if kill -0 "$pid" 2>/dev/null; then
            log "Une autre instance est déjà en cours (PID: $pid)"
            return 1
        else
            log "Suppression du verrou obsolète (PID: $pid)"
            rm -f "$LOCK_FILE"
        fi
    fi

    echo $$ > "$LOCK_FILE"
    return 0
}

release_lock() {
    rm -f "$LOCK_FILE"
}

# Traite une vidéo
process_video() {
    local input_file="$1"
    local filename=$(basename "$input_file")
    local name_without_ext="${filename%.mp4}"

    log "========================================="
    log "Traitement: $filename"
    log "========================================="

    # Déplace vers le dossier de traitement
    local processing_file="$PROCESSING_DIR/$filename"
    if ! mv "$input_file" "$processing_file"; then
        error "Impossible de déplacer le fichier vers processing"
        return 1
    fi

    # Vérifie que le fichier est stable
    log "Vérification de la stabilité du fichier..."
    local attempts=0
    local max_attempts=5  # Réduit de 10 à 5 pour 10 secondes total

    while ! is_file_stable "$processing_file"; do
        attempts=$((attempts + 1))
        if [ $attempts -gt $max_attempts ]; then
            error "Timeout: le fichier n'est pas stable après $((max_attempts * 2)) secondes"
            error "Cela peut arriver si:"
            error "1. Le fichier est corrompu"
            error "2. Le fichier est encore en cours de copie"
            error "3. Le système de fichiers est lent"
            mv "$processing_file" "$ERROR_DIR/"
            return 1
        fi
        log "Tentative $attempts/$max_attempts - Fichier potentiellement en cours d'écriture, attente..."
    done

    log "Fichier stable, début du traitement"

    # Prépare les chemins de sortie
    local output_subdir="$OUTPUT_DIR/$name_without_ext"
    local index_file="$output_subdir/${name_without_ext}.m3u8"

    mkdir -p "$output_subdir"

    # Lance la segmentation
    log "Lancement de la segmentation..."
    log "Commande: $SEGMENTER \"$processing_file\" \"$output_subdir\" \"$index_file\" \"segment\" \"$EXTENSION\" $SEGMENT_DURATION $MAX_SEGMENTS"

    if "$SEGMENTER" "$processing_file" "$output_subdir" "$index_file" "segment" "$EXTENSION" $SEGMENT_DURATION $MAX_SEGMENTS >> "$LOG_FILE" 2>&1; then
        log "Segmentation réussie: $filename"

        # Déplace vers done
        mv "$processing_file" "$DONE_DIR/"

        # Crée un fichier info
        cat > "$output_subdir/info.txt" <<EOF
Fichier source: $filename
Date de traitement: $(date '+%Y-%m-%d %H:%M:%S')
Durée segments: ${SEGMENT_DURATION}s
Index: ${name_without_ext}.m3u8
EOF

        log "Fichiers générés dans: $output_subdir"
        log "Nombre de segments: $(ls -1 "$output_subdir"/segment-*.ts 2>/dev/null | wc -l)"
        return 0
    else
        error "Échec de la segmentation: $filename"
        mv "$processing_file" "$ERROR_DIR/"
        return 1
    fi
}

# Traite tous les MP4 du dossier
process_all_videos() {
    local count=0
    local success=0
    local failed=0

    log "Recherche de vidéos à traiter dans: $WATCH_DIR"

    # Parcourt tous les fichiers MP4
    for video in "$WATCH_DIR"/*.mp4; do
        # Vérifie si le fichier existe (le glob peut ne rien trouver)
        if [ ! -f "$video" ]; then
            continue
        fi

        count=$((count + 1))

        if process_video "$video"; then
            success=$((success + 1))
        else
            failed=$((failed + 1))
        fi
    done

    if [ $count -gt 0 ]; then
        log "========================================="
        log "Résumé: $count vidéo(s) traitée(s)"
        log "Succès: $success | Échecs: $failed"
        log "========================================="
    else
        log "Aucune vidéo à traiter"
    fi
}

# Mode surveillance continue (optionnel)
watch_mode() {
    log "Mode surveillance activé (Ctrl+C pour arrêter)"

    while true; do
        process_all_videos
        sleep 30  # Vérifie toutes les 30 secondes
    done
}

# Nettoie les anciens fichiers traités
cleanup_old_files() {
    local days=${1:-7}
    log "Nettoyage des fichiers de plus de $days jours..."

    find "$DONE_DIR" -name "*.mp4" -mtime +$days -delete 2>/dev/null
    find "$ERROR_DIR" -name "*.mp4" -mtime +$days -delete 2>/dev/null

    log "Nettoyage terminé"
}

#############################################
# Script principal
#############################################

main() {
    # Initialisation
    init_directories

    # Vérifie les dépendances
    if [ ! -x "$SEGMENTER" ]; then
        error "Le binaire $SEGMENTER n'existe pas ou n'est pas exécutable"
        error "Lancez d'abord: bash install.sh"
        exit 1
    fi

    # Vérifie le lock
    if ! acquire_lock; then
        exit 1
    fi

    # Trap pour libérer le lock à la sortie
    trap release_lock EXIT INT TERM

    # Parse les arguments
    case "${1:-}" in
        watch)
            watch_mode
            ;;
        cleanup)
            cleanup_old_files "${2:-7}"
            ;;
        *)
            process_all_videos
            ;;
    esac
}

# Affiche l'aide
if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    cat <<EOF
Usage: $0 [OPTIONS]

OPTIONS:
  (aucun)       Traite tous les MP4 du dossier une seule fois
  watch         Mode surveillance continue (boucle infinie)
  cleanup [N]   Nettoie les fichiers de plus de N jours (défaut: 7)
  -h, --help    Affiche cette aide

CONFIGURATION:
  Éditez les variables en haut du script pour changer:
  - WATCH_DIR: dossier surveillé
  - OUTPUT_DIR: dossier de sortie
  - SEGMENT_DURATION: durée des segments
  - etc.

EXEMPLES:
  $0                    # Traite une fois
  $0 watch              # Surveillance continue
  $0 cleanup 14         # Nettoie les fichiers de +14 jours

LOGS:
  $LOG_FILE
EOF
    exit 0
fi

# Lance le script
main "$@"
{{- define "selectorLabels" -}}
app.name:  {{ .Chart.Name }}
app.instance:  {{ .Values.application.instance }}
app.version: {{ .Chart.AppVersion | quote }}
{{- end }}

{{- define "labels" -}}
{{ include "selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Values.application.instance }}
{{- end }}
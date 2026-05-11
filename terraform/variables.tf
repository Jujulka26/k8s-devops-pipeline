variable "project_id" {
  description = "GCP Project ID"
  default     = "k8s-devops-pipeline"
}

variable "region" {
  description = "GCP region"
  default     = "us-central1"
}

variable "cluster_name" {
  description = "GKE cluster name"
  default     = "k8s-devops-cluster"
}
